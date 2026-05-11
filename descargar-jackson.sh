#!/usr/bin/env bash
#
# descargar-jackson.sh
#
# Descarga los tres JARs de Jackson necesarios para las etapas 3 y 5
# y los copia a las carpetas libs/ correspondientes.
#
# Uso:
#   bash descargar-jackson.sh
#
# Requiere: curl (instalado por defecto en macOS, Linux y Windows 10+).

set -e

JACKSON_VERSION="2.17.0"
BASE_URL="https://repo1.maven.org/maven2/com/fasterxml/jackson/core"

# Carpetas destino
DESTINOS=(
    "03-sockets-jackson/libs"
    "05-fastapi/cliente-java/libs"
)

# JARs a descargar
JARS=(
    "jackson-core-${JACKSON_VERSION}.jar"
    "jackson-annotations-${JACKSON_VERSION}.jar"
    "jackson-databind-${JACKSON_VERSION}.jar"
)

# URLs (las tres viven bajo /com/fasterxml/jackson/core/<artefacto>/<version>/)
url_de() {
    local jar="$1"
    local artefacto
    artefacto=$(echo "$jar" | sed "s/-${JACKSON_VERSION}.jar//")
    echo "${BASE_URL}/${artefacto}/${JACKSON_VERSION}/${jar}"
}

# Descargar a una carpeta temporal una sola vez
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

echo "Descargando Jackson ${JACKSON_VERSION}..."
for jar in "${JARS[@]}"; do
    url="$(url_de "$jar")"
    echo "  - $jar"
    curl -fsSL -o "${TMP_DIR}/${jar}" "$url"
done

echo
echo "Copiando a las carpetas libs/..."
for destino in "${DESTINOS[@]}"; do
    mkdir -p "$destino"
    for jar in "${JARS[@]}"; do
        cp "${TMP_DIR}/${jar}" "${destino}/"
    done
    echo "  - ${destino}/  ($(ls "$destino" | grep -c '.jar$') jars)"
done

echo
echo "Listo. Ahora puede compilar las etapas 3 y 5 con:"
echo "  javac -cp \"libs/*\" ..."
