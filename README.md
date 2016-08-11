# Ограниченная Машина Больцмана на Apache Giraph
Данный проект является реализацией Ограниченной Машины Больцмана (RBM) под фреймворком Apache Giraph для распределенной обработки графовых данных. Подробнее про RBM можно прочитать в [статье Джеффри Хинтона](https://www.cs.toronto.edu/~hinton/absps/guideTR.pdf) (англ) и в [статье Павла Нестерова](https://habrahabr.ru/post/159909/).

### Сборка
Для сборки проекта необходим [maven](https://maven.apache.org/) версии не менее 3. Все параметры сборки указаны в файле `pom.xml`. Текущая версия рассчитана на Hadoop 2.6 от Cloudera, однако это можно настроить в `pom.xml`. Чтобы собрать проект, достаточно выполнить скрипт `build.sh`. 

### Параметры запуска
Для запуска используется скрипт `runner.sh`. В нем можно отредактировать следующие параметры запуска:
- `maxEpochNumber` - количество повторений обучения на всех данных;
- `maxBatchNumber` - количество батчей, используемых для обучения;
- `maxStepInSample` - количество прогонов батча с видимого слоя на скрытый и обратно;
- `learningRate` - темп обучения;
- `visibleLayerSize` - количество нейронов на видимом слое;
- `hiddenLayerSize` - количество нейронов на скрытом слое;
- `inputPath` - путь до входных файлов (в Hadoop FS) в формате `hdfs://fs.defaultFS/path`. `fs.defaultFS` можно найти в `HADOOP_HOME/conf/core-site.xml`. Обычно `HADOOP_HOME` - это `/etc/hadoop/`;
- `useSampling` - если true, производит семплирование на скрытом слое.
Помимо этого, нужно через параметр `vip` указать путь к файлу `RBM.txt`, о котором будет сказано ниже. Также необходимо указать путь, по которому будет сохранен полученный граф. Он передается через параметр `vop` в скрипте `runner.sh`.

### Формат входных данных
![input data structure](https://raw.githubusercontent.com/AlexandrShcherbakov/RBM/master/input%20data%20structure.jpg)

В текущей реализации предполагается, что все файлы лежат в папке, переданной параметром `inputPath`. В этой папке находятся подпапки для каждого нейрона видимого слоя с названиями - числами от 0 до `visibleLayerSize - 1`. В каждой из них находятся текстовые файлы, соответствующие батчам, с номерами от 0 до `maxBatchNumber - 1`. В этих файлах находятся JSON-списки вещественных чисел длины, соответствующей размеру батча. Схема размещения файлов приведена выше. 
Также в корневом каталоге проекта находится Python-скрипт `create_batches_mnist.py`, который раскладывает по папкам стандартный датасет MNIST. Потом полученные данные нужно из локальной файловой системы перенести в HDFS. Для этого достаточно выполнить скрипт `load_on_hdfs.sh`. 
*Примечание:* Создание и загрузка такого количества файлов может занять некоторое время. Для ускорения можно уменьшить число батчей или увеличить их размер в скрипте `create_batches_mnist.py`.
Для создания нейросети необходима одна начальная вершина, которая создает все остальные. Она записана в файле `RBM.txt`, который лежит в корневой папке. Его нужно загрузить в HDFS и указать путь к нему в `runner.sh` параметром `vip`. 

### Запуск
Если Вы собрали все исходники, указали все необходимые параметры в `runner.sh` и загрузили входные данные (включая `RBM.txt`) в HDFS, то теперь для запуска достаточно выполнить `runner.sh`.