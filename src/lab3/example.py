import numpy as np

def sigmoid(x):
    return 1 / (1 + np.exp(x))
x = np.load('x.npy')
y = np.load('y.npy')

theta_0 = np.load('theta_0.npy')
theta_1 = np.load('theta_1.npy')

idx = 1
image = x[idx]

xp = np.hstack((1, image))
res = np.dot(xp, theta_0.T)
a = sigmoid(res)

print(f"result {res}")
print(f"sigmoid {a}")
