import numpy as np

def sigmoid(x):
    return 1 / (1 + np.exp(-x))

x = np.load('x.npy') # 5000 images
y = np.load('y.npy')

theta_0 = np.load('theta_0.npy')
theta_1 = np.load('theta_1.npy')

idx = 1
image = x[idx]

# Calculer sur le FPGA
xp = np.hstack((1, image)) # Ajouter 1 pour le biais
a = sigmoid(np.dot(xp,theta_0.T)) # Sortie de la couche 1
ap = np.hstack((1, a)) # Ajouter 1 pour le biais
b = sigmoid(np.dot(ap,theta_1.T)) # Sortie de la couche 2
print("FPGA Output:",b)

# Calculer dans le notebook
pred = b.argmax()
print("Prediction:", pred, "Label:", y[idx])