#!/bin/sh
# Executa o Gerenciador de Projetos no Linux. Requer Java 17 ou superior (JRE/JDK).
DIR="$(cd "$(dirname "$0")" && pwd)"

if ! command -v java >/dev/null 2>&1; then
    echo "Java não encontrado. Instale o JDK 17+ (ex.: sudo apt install openjdk-21-jre)." >&2
    exit 1
fi

VERSAO="$(java -version 2>&1 | sed -n '1s/.*version "\([0-9]*\).*/\1/p')"
if [ -n "$VERSAO" ] && [ "$VERSAO" -lt 17 ]; then
    echo "Java $VERSAO encontrado, mas é preciso 17 ou superior." >&2
    exit 1
fi

exec java -cp "$DIR/lib/*" br.edu.trabalho.gerenciadorprojetos.ui.Launcher "$@"
