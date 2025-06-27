<h1 align="center"> 
  🗺 Interpolação espacial (IDW)
</h1>

<p align="center">
  <a href="#-sobre-o-projeto">Sobre</a> •
  <a href="#-como-executar-o-projeto">Como executar</a> • 
  <a href="#-versões-do-algoritmo">Versões do algoritmo</a> • 
  <a href="#-profiler">Profiler</a>
</p>

<br>

## 💻 Sobre o projeto

Projeto desenvolvido na disciplina Programação Concorrente do curso de Tecnologia da Informação (IMD/UFRN) implementando o algortimo de interpolação espacial.

A interpolação espacial é o processo de estimativa de valores desconhecidos em determinados pontos do espaço com base em informações conhecidas do ambiente. Os dados utilizados por essa técnica consistem em um conjunto de coordenadas espaciais, cada uma com um ou mais valores associados, como temperatura e precipitação, por exemplo.

Dentre os diversos métodos de interpolação espacial existentes, a aplicação desenvolvida utilizou o método Inverso da Distância à Potência (Inverse Distance Weighted – IDW). Esse algoritmo analisa todos os pontos do ambiente e atribui um peso (ou ponderação) a cada coordenada, de acordo com sua proximidade em relação ao ponto de interesse. Quanto mais próximo for o ponto conhecido, maior será seu peso e maior a influência sobre o cálculo da estimativa do valor do atributo.

---

## 🚀 Como executar o projeto

Para executar o projeto, siga os seguintes passos:

1. É preciso ter o Java JDK 24 instalado na sua máquina.
2. Para executar pelo terminal:

```bash
# Clone este repositório
$ git clone git@github.com:fabianapduarte/idw-java.git

# Acesse a pasta do repositório
$ cd idw-java

# Gere o dataset do algoritmo
$ java -cp bin utils.GenerateData

# Execute o algoritmo, passando as coordenadas para cálculo da interpolação espacial
# java -cp bin idw.<Versão do algoritmo> <x> <y>
# Exemplo:
$ java -cp bin idw.IDWInterpolationV0 1 1

# Para executar e gerar o profiler da execução na sua máquina
# java -XX:StartFlightRecording=duration=90s,filename=profiler/v0/idw-v0-g1.jfr -cp bin idw.<Versão do algoritmo> <x> <y>
# Exemplo:
$ java -XX:StartFlightRecording=duration=90s,filename=profiler/v0/idw-v0-g1.jfr -cp bin idw.IDWInterpolationV0 1 1
```

3. Para executar pelo VSCode:

- Abra o projeto no VSCode;
- Para editar algum parâmetro de execução, edite o arquivo `.vscode/launch.json`;
- Execute o código com Ctrl + F5.

---

## 🗂 Versões do algoritmo

- `IDWInterpolationBaseline`: Versão serial e não otimizada do algoritmo;
- `IDWInterpolationV0`: Versão serial com otimizações para melhorar uso de memória;
- `IDWInterpolationV1`: Versão com paralelismo usando platform threads (sem tratamento de condição de corrida);
- `IDWInterpolationV2`: Versão com paralelismo usando virtual threads (sem tratamento de condição de corrida);
- `IDWInterpolationV3`: Versão com paralelismo usando platform threads e mutex;
- `IDWInterpolationV4`: Versão com paralelismo usando platform threads e variáveis atômicas;
- `IDWInterpolationV5`: Versão com paralelismo usando platform threads e `ReentrantLock` injusto;
- `IDWInterpolationV6`: Versão com paralelismo usando platform threads e `ReentrantLock` justo.

---

## 📈 Profiler

Para cada versão implementada em Java, foram gerados arquivos de profiling utilizando as ferramentas Java Flight Recorder (JFR) e JDK Mission Control (JMC). Além disso, as análises foram geradas para os seguintes coletores de lixo: G1, Serial, Parallel e ZGC.

Para visualizar os resultados, instale o JDK Mission Control e abra os arquivos de profiling presentes na pasta profiler.
