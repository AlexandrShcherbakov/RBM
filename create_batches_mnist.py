from __future__ import print_function
import numpy as np
from sklearn.datasets import fetch_mldata
import os

print('Downloading MNIST')
data = fetch_mldata('MNIST original')
print('Writing batches')
X = []
for i in range(10):
    X.append(data.data[data.target == i])
minlen = min(map(lambda x: x.shape[0], X))
for i in range(10):
    X[i] = X[i][:minlen]
X = np.array(X) > 128

batch_size = 100
neuron_cnt = X.shape[-1]
batch_per_class = batch_size // 10
batch_cnt = X.shape[1] // batch_per_class

path = './mnist/%d/%d.txt'
if not os.path.exists('./mnist'):
    os.mkdir('./mnist')
for i in range(neuron_cnt):
    if not os.path.exists('./mnist/%d' % i):
        os.mkdir('./mnist/%d' % i)
    for j in range(batch_cnt):
        x = X[:, j * batch_per_class : (j + 1) * batch_per_class, i].ravel()
        s = '[' + ','.join(map(lambda x: '%g' % x, x)) + ']\n'
        open(path % (i, j), 'w').write(s)