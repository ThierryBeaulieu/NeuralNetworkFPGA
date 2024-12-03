import numpy as np

def sigmoid(x):
    return 1 / (1 + np.exp(-x))


# Deux sigmoid à produire
# sigmoid 1 [5, 11]
# sigmoid 2 [4, 12]

# On n'a qu'à générer les 65536 possible outcome pour la sigmoid1
# et pour la sigmoid 2, en théorie, c'est juste 128Kb que ça va demander.
# à moins que j'utilisation la même sigmoide [5, 11] je pense que c'est ça que je vais faire,
# Il va juste falloir que je divise par deux les éléments avant la sigmoide 2.