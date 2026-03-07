package com.example.myapplication.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.AnalysisResult
import com.example.myapplication.network.ApiMapper
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val result: AnalysisResult) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}

class AnalysisViewModel : ViewModel() {

    private val _state = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val state: StateFlow<AnalysisState> = _state.asStateFlow()

    fun analyze(input: String) {
        viewModelScope.launch {
            _state.value = AnalysisState.Loading
            try {
                // Determine if input is a URL or raw text
                val isUrl = input.startsWith("http://") || input.startsWith("https://")
                
                // Create RequestBody for Multipart
                val requestBody = input.toRequestBody("text/plain".toMediaTypeOrNull())
                
                val apiResponse = if (isUrl) {
                    RetrofitClient.api.analyze(url = requestBody)
                } else {
                    RetrofitClient.api.analyze(text = requestBody)
                }
                
                val internalResult = ApiMapper.mapToInternal(apiResponse)
                _state.value = AnalysisState.Success(internalResult)
            } catch (e: Exception) {
                _state.value = AnalysisState.Error(e.localizedMessage ?: "Analysis failed")
            }
        }
    }

    fun analyzeFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = AnalysisState.Loading
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: throw Exception("Could not read file")
                inputStream.close()

                val requestFile = bytes.toRequestBody(
                    contentResolver.getType(uri)?.toMediaTypeOrNull(),
                    0,
                    bytes.size
                )
                
                val body = MultipartBody.Part.createFormData("file", "upload_file", requestFile)
                
                val apiResponse = RetrofitClient.api.analyze(file = body)
                
                val internalResult = ApiMapper.mapToInternal(apiResponse)
                _state.value = AnalysisState.Success(internalResult)
            } catch (e: Exception) {
                _state.value = AnalysisState.Error(e.localizedMessage ?: "File analysis failed")
            }
        }
    }

    fun resetState() {
        _state.value = AnalysisState.Idle
    }
}
