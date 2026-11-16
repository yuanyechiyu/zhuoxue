from fastapi import FastAPI
import uvicorn

app = FastAPI(title="琢学AI服务",version="0.0.1")

@app.get("/health")
def health():
    return {"status": "ok","service": "zhuoxue-ai"}

if __name__ == "__main__":
    uvicorn.run(app,host="127.0.0.1",port=8000)