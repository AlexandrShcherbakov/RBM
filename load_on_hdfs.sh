echo 'Create input dir'
hdfs dfs -mkdir input
echo 'Copy RBM.txt to input/RBM.txt'
hdfs dfs -put ./RBM.txt input

echo 'Upload mnist'
hdfs dfs -mkdir mnist
echo 'Load 0-9'
hdfs dfs -put mnist/[0-9] mnist/
echo 'Load 10-49'
hdfs dfs -put mnist/[1-4][0-9] mnist/
echo 'Load 50-99'
hdfs dfs -put mnist/[5-9][0-9] mnist/
echo 'Load 100-149'
hdfs dfs -put mnist/1[0-4][0-9] mnist/
echo 'Load 150-199'
hdfs dfs -put mnist/1[5-9][0-9] mnist/
echo 'Load 200-249'
hdfs dfs -put mnist/2[0-4][0-9] mnist/
echo 'Load 250-299'
hdfs dfs -put mnist/2[5-9][0-9] mnist/
echo 'Load 300-349'
hdfs dfs -put mnist/3[0-4][0-9] mnist/
echo 'Load 350-399'
hdfs dfs -put mnist/3[5-9][0-9] mnist/
echo 'Load 400-449'
hdfs dfs -put mnist/4[0-4][0-9] mnist/
echo 'Load 450-499'
hdfs dfs -put mnist/4[5-9][0-9] mnist/
echo 'Load 500-549'
hdfs dfs -put mnist/5[0-4][0-9] mnist/
echo 'Load 550-599'
hdfs dfs -put mnist/5[5-9][0-9] mnist/
echo 'Load 600-649'
hdfs dfs -put mnist/6[0-4][0-9] mnist/
echo 'Load 650-699'
hdfs dfs -put mnist/6[5-9][0-9] mnist/
echo 'Load 700-749'
hdfs dfs -put mnist/7[0-4][0-9] mnist/
echo 'Load 750-799'
hdfs dfs -put mnist/7[5-9][0-9] mnist/
