# ai/ai-service/main.py
from fastapi import FastAPI
import py_eureka_client.eureka_client as eureka_client
import asyncio

app = FastAPI()

@app.on_event("startup")
async def register_with_eureka():
    try:
        await eureka_client.init_async(
            eureka_server="http://eureka-server:8761/eureka",
            app_name="AI-SERVICE",
            instance_port=8000,
        )
        print("✅ AI-SERVICE registrado en Eureka")
    except Exception as e:
        print(f"⚠️ No se pudo conectar a Eureka: {e}")

@app.get("/health")
def health():
    return {"status": "UP"}
