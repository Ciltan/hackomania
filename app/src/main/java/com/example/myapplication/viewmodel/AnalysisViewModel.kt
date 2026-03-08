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

    private val _historyResults = MutableStateFlow<List<AnalysisResult>>(emptyList())
    val historyResults: StateFlow<List<AnalysisResult>> = _historyResults.asStateFlow()

    private val _isHistoryLoading = MutableStateFlow(false)
    val isHistoryLoading: StateFlow<Boolean> = _isHistoryLoading.asStateFlow()

    private val _translatedText = MutableStateFlow<String?>(null)
    val translatedText: StateFlow<String?> = _translatedText.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _chatAnswer = MutableStateFlow<String?>(null)
    val chatAnswer: StateFlow<String?> = _chatAnswer.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    init {
        fetchHistory()
    }

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
                fetchHistory() // Refresh history after analysis
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
                fetchHistory() // Refresh history after analysis
            } catch (e: Exception) {
                _state.value = AnalysisState.Error(e.localizedMessage ?: "File analysis failed")
            }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _isHistoryLoading.value = true
            try {
                val apiHistory = RetrofitClient.api.getHistory()
                _historyResults.value = apiHistory.map { ApiMapper.mapToInternal(it) }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isHistoryLoading.value = false
            }
        }
    }

    fun translateArticle(text: String, targetLanguage: String) {
        viewModelScope.launch {
            _isTranslating.value = true
            try {
                val langBody = targetLanguage.toRequestBody("text/plain".toMediaTypeOrNull())
                val isUrl = text.startsWith("http://") || text.startsWith("https://")
                
                val response = if (isUrl) {
                    val urlBody = text.toRequestBody("text/plain".toMediaTypeOrNull())
                    RetrofitClient.api.translate(url = urlBody, targetLanguage = langBody)
                } else {
                    val textBody = text.toRequestBody("text/plain".toMediaTypeOrNull())
                    RetrofitClient.api.translate(text = textBody, targetLanguage = langBody)
                }
                
                _translatedText.value = response.translatedText
            } catch (e: Exception) {
                _translatedText.value = "Translation failed: ${e.localizedMessage}"
            } finally {
                _isTranslating.value = false
            }
        }
    }

    fun askFollowUp(claim: String, analysisSummary: String, question: String) {
        viewModelScope.launch {
            _isChatLoading.value = true
            _chatAnswer.value = null
            try {
                val claimBody = claim.toRequestBody("text/plain".toMediaTypeOrNull())
                val summaryBody = analysisSummary.toRequestBody("text/plain".toMediaTypeOrNull())
                val questionBody = question.toRequestBody("text/plain".toMediaTypeOrNull())
                val response = RetrofitClient.api.chat(
                    claim = claimBody,
                    analysisSummary = summaryBody,
                    question = questionBody
                )
                _chatAnswer.value = response.answer
            } catch (e: Exception) {
                _chatAnswer.value = "Error: ${e.localizedMessage}"
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun resetState() {
        _state.value = AnalysisState.Idle
        _translatedText.value = null
        _chatAnswer.value = null
    }
}
