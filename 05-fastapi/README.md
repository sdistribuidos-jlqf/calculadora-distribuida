# Etapa 5 — FastAPI + cliente Java con Jackson

Última etapa de la evolución: el servidor habla **HTTP + JSON**, un estándar universal. Cualquier cliente —Java, Python, JavaScript, curl, Postman— puede consumirlo. Es lo que hoy llamamos un servicio REST.

Respecto a la Etapa 3 (sockets multihilo + Jackson), aquí cambia el **transporte**: en lugar de TCP crudo usamos HTTP, lo que aporta verbos (`POST`), rutas (`/operar`), códigos de estado (200, 400, 500) y un ecosistema gigante de herramientas (curl, Swagger, Postman, etc.).

## Estructura

```
05-fastapi/
├── servidor/         -> FastAPI (Python)
.
```

## Servidor FastAPI

### Requisitos

- Python 3.9+
- pip

### Instalación

```bash
cd 05-fastapi/servidor
python -m venv venv
source venv/bin/activate          # En Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### Ejecución

```bash
uvicorn main:app --reload
```

El servidor queda disponible en `http://localhost:8000`.

### Documentación interactiva (Swagger)

FastAPI genera documentación OpenAPI **automáticamente**. Visite:

- `http://localhost:8000/docs` — Swagger UI: probar el endpoint desde el navegador.
- `http://localhost:8000/redoc` — ReDoc: documentación más legible.

Esta es una ventaja enorme respecto a las etapas anteriores: no escribimos una línea de documentación y ya tenemos un cliente web funcional.

### Probar con curl

```bash
# Suma
curl -X POST http://localhost:8000/operar \
     -H "Content-Type: application/json" \
     -d '{"operacion":"suma","a":3,"b":4}'
# {"operacion":"suma","a":3.0,"b":4.0,"resultado":7.0}

# División entre cero (devuelve HTTP 400)
curl -i -X POST http://localhost:8000/operar \
     -H "Content-Type: application/json" \
     -d '{"operacion":"division","a":10,"b":0}'
# HTTP/1.1 400 Bad Request
# {"detail":"Division entre cero"}

# Operación inválida (validación automática de Pydantic)
curl -i -X POST http://localhost:8000/operar \
     -H "Content-Type: application/json" \
     -d '{"operacion":"raiz","a":9,"b":0}'
# HTTP/1.1 422 Unprocessable Entity
# {"detail":[{"loc":["body","operacion"],"msg":"...","type":"..."}]}
```

## Por qué este enfoque ganó

| Dimensión | Sockets (1-3) | RMI (4) | FastAPI + JSON (5) |
|---|---|---|---|
| Independencia de lenguaje | manual | no | **sí** |
| Atravesabilidad de red | sí | difícil | **sí** |
| Documentación automática | no | no | **sí (Swagger)** |
| Tipado en el contrato | no/sí (Etapa 3 con DTOs) | sí | sí (con OpenAPI) |
| Herramientas externas (curl, Postman, generadores de SDK) | no | no | **enorme** |

El precio es que perdemos algo del confort de RMI (la llamada deja de "parecer local"). Lo ganado: interoperabilidad total. Hoy es el patrón dominante para integración entre servicios web.
