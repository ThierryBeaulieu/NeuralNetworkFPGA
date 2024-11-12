# NeuralNetworkFPGA

Deep neural networks are becoming more and more used though the years. Neural networks often have two implementations. The first one is build and trained on GPUs. The weights and the bias found can then be used on a model synthesized on FPGAs. Those neural network doesn't require a large amount of energy and are quite efficient.

The purpose of this repository is to build different types of neural networks.

## Execute
To run the project, use the following command:
```
sbt run 
```

## Tests
To run the tests, use the following command:
```
sbt test
```

If you would like to only run the tests of the project, feel free to use:
```
sbt testOnly project.LFSRSpec
```
