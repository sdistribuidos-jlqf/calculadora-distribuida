"""
ETAPA 4: Servidor REST con FastAPI.

Salto cualitativo respecto a las etapas anteriores:
 - El protocolo es HTTP, universal y atravesable por firewalls/proxies.
 - El formato es JSON, legible por humanos e independiente del lenguaje.
 - FastAPI valida automáticamente los tipos gracias a Pydantic y genera
   documentación OpenAPI/Swagger en /docs sin que escribamos nada extra.

El servidor expone un único endpoint POST /operar que recibe un JSON
con la operación y los operandos, y devuelve un JSON con el resultado
o el error. Cualquier cliente que hable HTTP+JSON puede consumirlo:
Java (con Jackson), Python, JavaScript, curl, Postman...
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import Literal

app = FastAPI(
    title="Calculadora Distribuida",
    description="Servicio REST de calculadora — Etapa 4 de la evolución de sistemas distribuidos.",
    version="1.0.0",
)


class SolicitudOperacion(BaseModel):
    """Modelo de la petición. Pydantic valida tipos automáticamente."""
    operacion: Literal["suma", "resta", "multiplicacion", "division"]
    a: float = Field(..., description="Primer operando")
    b: float = Field(..., description="Segundo operando")


class RespuestaOperacion(BaseModel):
    """Modelo de la respuesta exitosa."""
    operacion: str
    a: float
    b: float
    resultado: float


@app.get("/")
def raiz():
    return {
        "servicio": "Calculadora Distribuida",
        "version": "1.0.0",
        "documentacion": "/docs",
    }


@app.post("/operar", response_model=RespuestaOperacion)
def operar(req: SolicitudOperacion):
    """
    Ejecuta la operación solicitada.

    Pydantic ya validó que `operacion` es uno de los valores permitidos
    y que `a` y `b` son numéricos. Aquí solo aplicamos la lógica.
    """
    if req.operacion == "suma":
        resultado = req.a + req.b
    elif req.operacion == "resta":
        resultado = req.a - req.b
    elif req.operacion == "multiplicacion":
        resultado = req.a * req.b
    elif req.operacion == "division":
        if req.b == 0:
            # HTTPException se traduce a una respuesta JSON con el código
            # HTTP indicado. El cliente la verá como un error 400.
            raise HTTPException(status_code=400, detail="Division entre cero")
        resultado = req.a / req.b
    else:
        # Esto no debería ocurrir gracias al Literal, pero queda como red de seguridad.
        raise HTTPException(status_code=400, detail="Operacion desconocida")

    return RespuestaOperacion(
        operacion=req.operacion,
        a=req.a,
        b=req.b,
        resultado=resultado,
    )


if __name__ == "__main__":
    # Permite ejecutar el servidor con: python main.py
    # En producción se usa: uvicorn main:app --host 0.0.0.0 --port 8000
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
