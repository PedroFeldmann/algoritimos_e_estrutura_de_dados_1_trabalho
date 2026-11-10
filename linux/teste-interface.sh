#!/bin/bash
# Teste de interface do executável Linux, usando o app como um usuário faria:
#   1. abre o app (sem contas) -> tela "Criar conta"
#   2. cria a conta e entra    -> tela "Dashboard"
#   3. fecha e abre de novo     -> tela "Entrar" (conta foi salva em disco)
#   4. faz login                -> tela "Dashboard"
# Tira um print de cada etapa em target/prints/ e falha se alguma tela não aparecer
# ou se o log do app tiver exceção.
# Requer: Xvfb, xdotool, imagemagick (import). Uso: bash linux/teste-interface.sh
set -u
APP="target/dist/GerenciadorDeProjetos/bin/GerenciadorDeProjetos"
PRINTS="target/prints"
LOG="$HOME/.gerenciador-projetos/log.txt"
mkdir -p "$PRINTS"
rm -rf "$HOME/.gerenciador-projetos"

Xvfb :99 -screen 0 1280x800x24 >/dev/null 2>&1 &
XVFB=$!
export DISPLAY=:99
sleep 2
openbox >/dev/null 2>&1 &
sleep 1

falhar() {
    echo "FALHOU: $1"
    import -window root "$PRINTS/erro.png" 2>/dev/null
    echo "----- log do app -----"; cat "$LOG" 2>/dev/null
    kill "$PID" "$XVFB" 2>/dev/null
    exit 1
}

# espera até 40 s por uma janela cujo título contenha $1; devolve o id dela
esperar_janela() {
    for _ in $(seq 1 40); do
        WID=$(xdotool search --onlyvisible --name "$1" 2>/dev/null | head -n1)
        if [ -n "$WID" ]; then
            sleep 2   # deixa a tela terminar de desenhar
            return 0
        fi
        sleep 1
    done
    return 1
}

focar() {
    xdotool windowactivate --sync "$WID" 2>/dev/null || xdotool windowfocus --sync "$WID"
    sleep 1
}

digitar() { xdotool type --delay 60 "$1"; }
tecla() { xdotool key "$1"; sleep 0.3; }

# ---------- 1. primeira abertura ----------
"$APP" & PID=$!
esperar_janela "Criar conta" || falhar "tela 'Criar conta' não apareceu"
LARGURA=$(xdotool getwindowgeometry "$WID" | sed -n 's/.*Geometry: \([0-9]*\)x.*/\1/p')
echo "largura da janela: $LARGURA"
[ "${LARGURA:-0}" -ge 700 ] || falhar "janela abriu pequena demais (${LARGURA}px)"
echo "OK 1/4: tela 'Criar conta' apareceu"
import -window root "$PRINTS/1-criar-conta.png"

# ---------- 2. cria a conta ----------
focar
digitar "teste";    tecla Tab
digitar "Teste CI"; tecla Tab
digitar "senha123"; tecla Tab
digitar "senha123"; tecla Return
esperar_janela "Dashboard" || falhar "Dashboard não abriu depois de criar a conta"
echo "OK 2/4: conta criada e Dashboard aberto"
import -window root "$PRINTS/2-dashboard.png"
[ -f "$HOME/.gerenciador-projetos/usuarios.json" ] || falhar "usuarios.json não foi gravado"

# ---------- 3. fecha e abre de novo ----------
kill "$PID"; wait "$PID" 2>/dev/null
"$APP" & PID=$!
esperar_janela "Entrar" || falhar "tela 'Entrar' não apareceu na segunda abertura"
echo "OK 3/4: na segunda abertura pediu login (conta ficou salva)"
import -window root "$PRINTS/3-entrar.png"

# ---------- 4. login ----------
focar
digitar "teste";    tecla Tab
digitar "senha123"; tecla Return
esperar_janela "Dashboard" || falhar "Dashboard não abriu depois do login"
echo "OK 4/4: login feito e Dashboard aberto"
import -window root "$PRINTS/4-dashboard-apos-login.png"

kill "$PID" 2>/dev/null; wait "$PID" 2>/dev/null
echo "----- log do app -----"; cat "$LOG"
if grep -qE "Exception|Error:" "$LOG"; then
    falhar "o log do app tem exceção"
fi
kill "$XVFB" 2>/dev/null
echo "TESTE DE INTERFACE PASSOU"
