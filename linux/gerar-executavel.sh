#!/bin/sh
# Gera, NO LINUX, uma versÃ£o autossuficiente (com Java embutido, sem exigir Java instalado).
# Requer: JDK 17+ com jpackage e Maven. O jpackage nÃ£o faz compilaÃ§Ã£o cruzada,
# por isso este script precisa rodar numa mÃ¡quina Linux.
#
# Uso (a partir da raiz do repositÃ³rio):  sh linux/gerar-executavel.sh
set -e

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$RAIZ"

rm -rf target/libs target/dist
mvn -q dependency:copy-dependencies -DoutputDirectory=target/libs -DincludeScope=runtime
mvn -q package -DskipTests
cp target/gerenciador-projetos-1.0-SNAPSHOT.jar target/libs/

jpackage --type app-image \
    --input target/libs \
    --dest target/dist \
    --name GerenciadorDeProjetos \
    --main-jar gerenciador-projetos-1.0-SNAPSHOT.jar \
    --main-class br.edu.trabalho.gerenciadorprojetos.ui.Launcher \
    --app-version 1.0 \
    --add-modules java.desktop,java.prefs,jdk.unsupported

tar -czf linux/GerenciadorDeProjetos-linux-standalone.tar.gz -C target/dist GerenciadorDeProjetos
echo "Pronto: linux/GerenciadorDeProjetos-linux-standalone.tar.gz"
echo "Para rodar: extraia e execute GerenciadorDeProjetos/bin/GerenciadorDeProjetos"
