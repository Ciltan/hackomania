from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import HTMLResponse
from fastapi.openapi.docs import get_swagger_ui_html
from api.endpoints import router as analysis_router
from core.database import Base, engine

from sqlalchemy.orm import Session
from models.analysis import AnalysisRecord

from datetime import datetime

# Create the database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Fact-Checking API",
    description="Backend API for verifying claims and URLs with LLM and lateral reading.",
    version="1.0.0",
    docs_url=None,
    redoc_url=None
)

@app.on_event("startup")
def startup_event():
    # Clear the search cache on every startup/reload
    with Session(engine) as session:
        try:
            num_deleted = session.query(AnalysisRecord).delete()
            session.commit()
            print(f"[{datetime.now().strftime('%H:%M:%S')}] CACHE CLEARED: Removed {num_deleted} entries from sql_app.db")
        except Exception as e:
            print(f"Error clearing cache: {e}")

@app.get("/docs", include_in_schema=False)
async def custom_swagger_ui_html():
    response = get_swagger_ui_html(
        openapi_url=app.openapi_url,
        title=app.title + " - Swagger UI",
        swagger_js_url="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js",
        swagger_css_url="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css",
        swagger_ui_parameters={"docExpansion": "list"},
    )
    
    # Inject CSS and JS into the HTML body
    content = response.body.decode("utf-8").replace(
        "</head>",
        """
        <style>
            /* Hide the redundant 'Send empty value' checkbox */
            .parameter__empty_value_toggle { display: none !important; }
            
            /* Style for the custom Clear button */
            .clear-btn {
                margin-left: 10px;
                padding: 2px 8px;
                background: #f44336;
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                font-size: 12px;
            }
            .clear-btn:hover { background: #d32f2f; }
        </style>
        <script>
            function addClearButtons() {
                const inputs = document.querySelectorAll('input[type="text"], textarea');
                inputs.forEach(input => {
                    if (input.nextSibling && input.nextSibling.className === 'clear-btn') return;
                    
                    const btn = document.createElement('button');
                    btn.innerText = 'Clear';
                    btn.className = 'clear-btn';
                    btn.onclick = (e) => {
                        e.preventDefault();
                        // Standard way to trigger React state updates from outside
                        const nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value").set;
                        const nativeTextAreaValueSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, "value").set;
                        
                        if (input.tagName === 'TEXTAREA') {
                            nativeTextAreaValueSetter.call(input, '');
                        } else {
                            nativeInputValueSetter.call(input, '');
                        }
                        
                        input.dispatchEvent(new Event('input', { bubbles: true }));
                    };
                    input.parentNode.insertBefore(btn, input.nextSibling);
                });
            }
            setInterval(addClearButtons, 1000);
        </script>
        </head>
        """
    )
    return HTMLResponse(content=content)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(analysis_router, prefix="/api/v1")

@app.get("/")
def read_root():
    return {"status": "ok", "message": "Fact-Checking API is running"}
