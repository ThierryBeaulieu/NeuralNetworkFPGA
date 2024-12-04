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
tmp = np.dot(xp,theta_0.T)
# print(f"hiddenLayer0 {tmp}")

a = sigmoid(tmp) # Sortie de la couche 1
# print(f"sigmoid0 {a}")

ap = np.hstack((1, a)) # Ajouter 1 pour le biais
sig0_Int8 = np.array([x * 2**7 for x in ap]).astype(np.int16)
print(f"sig0 {sig0_Int8}")

tmp2 = np.dot(ap,theta_1.T)

hiddenLayer2 = np.array([x * 2**14 for x in tmp2]).astype(np.int16)
# print(f"hiddenLayer1Float {tmp2}")
print(f"hiddenLayer1Float {tmp2}")

b = sigmoid(tmp2) # Sortie de la couche 2
# print(f"sig1 {b}")

#print("FPGA Output:",b)

# Calculer dans le notebook
pred = b.argmax()
print("Prediction:", pred, "Label:", y[idx])