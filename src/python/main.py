from abc import ABC, abstractmethod
import numpy as np
import matplotlib.pyplot as plt
from numpy.typing import NDArray
from neuralnetwork import Neuron
from neuralnetwork import B2ISBipolar
from neuralnetwork import B2SBipolar
from neuralnetwork import B2SUnipolar
from neuralnetwork import BitwiseOperatorAND
from neuralnetwork import CounterUnipolar
from neuralnetwork import NStanh


class Test:
    def B2ISTest0(self):
        print("### B2SITest0")
        weights = np.array([-2, -1, 0, 1, 2], dtype=np.int8)
        for i in range(0, len(weights)):
            b2IS = B2ISBipolar(2)
            stream = []
            for _ in range(0, 1024):
                stream.append(b2IS.tick(weights[i]))
            sum = 0
            for element in stream:
                sum = sum + element
            print(f"Weight {weights[i]} approx : {(sum / (len(stream)))}")

    def B2ISTest(self):
        print("### B2SITest")
        weights = np.array([-128, -64, -32, -16, 0, 16, 32, 64, 127], dtype=np.int8)
        for i in range(0, len(weights)):
            b2IS = B2ISBipolar(128)
            stream = []
            for _ in range(0, 1024):
                stream.append(b2IS.tick(weights[i]))
            sum = 0
            for element in stream:
                sum = sum + element
            print(f"Weight {weights[i]} approx : {sum / (len(stream))}")

    def B2STest(self):
        print("### B2STest")
        b2S = B2SUnipolar()
        pixels = np.array([0, 16, 32, 64, 128, 255], dtype=np.uint8)
        for i in range(0, len(pixels)):
            stream = []
            for _ in range(0, 1024):
                stream.append(b2S.tick(pixels[i]))
            ones = 0
            zeros = 0
            for element in stream:
                if element == 1:
                    ones = ones + 1
                else:
                    zeros = zeros + 1
            print(f"Pixel {pixels[i]} approx : {(ones / (ones + zeros)) * 256}")

    def BitwiseANDTest(self):
        print("### Bitwise Adder")
        b2S = B2SUnipolar()
        b2IS = B2ISBipolar(m=128)
        bitwiseAND = BitwiseOperatorAND()

        weights = np.array([-128, 0, 127], dtype=np.int8)
        pixels = np.array([0, 16, 32, 64, 128, 255], dtype=np.uint8)

        for i in range(0, len(pixels)):
            weight = weights[0]
            stream = []
            for _ in range(0, 1024):
                sBinary = b2S.tick(pixels[i] / 256)
                sInteger = b2IS.tick(weight)
                res = bitwiseAND.tick(sInteger, sBinary)
                stream.append(res)

            sum = 0
            for element in stream:
                sum = sum + element
            # Equivalent of W * x
            multiplicationResult = sum / len(stream)
            print(f"Pixel {pixels[i]} weight {weight} mul {multiplicationResult}")

    def UnipolarCounterTest(self):
        print("### Unipolar Counter test")
        B2S = B2SUnipolar()
        unipolarCounter = CounterUnipolar()

        pixels = np.array([0, 16, 32, 64, 128, 255], dtype=np.uint8)
        for i in range(0, len(pixels)):
            result = 0
            for _ in range(0, 1024):
                res = unipolarCounter.tick(B2S.tick(pixels[i]))
                if res is not None:
                    result = res
            print(f"Pixel {pixels[i]} result {result}")

    def NStanhTest1(self):
        """
        m = 2
        offset = 4
        n = 4
        """
        nStanh = NStanh(8)
        bipolar = B2ISBipolar(m=128)
        output = []
        input = np.arange(-128, 127, 1)
        s = []
        n = 8
        # practical model
        for i in range(0, len(input)):
            stream = []
            bipolarValues = []
            for _ in range(0, 1024):
                si1 = bipolar.tick(input[i])
                si = si1
                m = 128
                bipolarValues.append(si)
                stream.append(2 * nStanh.tick(si, n * m) - 1)
                bipolarValues.append(si)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            probability /= 64
            s.append(probability)
            output.append(sum)

        # theorical model
        th_input = np.arange(-2.0, 2.1, 0.1)
        th_ouput = np.tanh(n * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker="o", label="NStanh(s)")
            plt.plot(th_input, th_ouput, color="orange", label="tanh(s)")
            plt.xlabel("s")
            plt.ylabel("ouput")
            plt.legend()
            plt.title("Approximation de NStanh avec m = 2 et offset = 2")
            plt.show()

    def NStanhTest2(self):
        """
        m = 1
        offset = 0.5
        n = 2
        """
        nStanh = NStanh(0.5)
        bipolar = B2ISBipolar()
        output = []
        input = np.arange(-128, 127, 1)
        s = []
        # practical model
        for i in range(0, len(input)):
            stream = []
            bipolarValues = []
            for _ in range(0, 1024):
                si1 = bipolar.tick(input[i])
                si = si1
                m = 1
                bipolarValues.append(si)
                stream.append(2 * nStanh.tick(si, 2 * m) - 1)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            s.append(probability)
            output.append(sum)

        # theorical model
        th_input = np.arange(-2.0, 2.1, 0.1)
        th_ouput = np.tanh(2 * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker="o", label="NStanh(s)")
            plt.plot(th_input, th_ouput, color="orange", label="tanh(s)")
            plt.xlabel("s")
            plt.ylabel("ouput")
            plt.legend()
            plt.title("Approximation de NStanh avec m = 1 et offset = 0.5")
            plt.show()

    def NStanhTest3(self):
        """
        m = 4
        offset = 16
        n = 8
        """
        nStanh = NStanh(16)
        bipolar = B2ISBipolar()
        output = []
        input = np.arange(-128, 127, 1)
        s = []
        n = 8
        # practical model
        for i in range(0, len(input)):
            stream = []
            bipolarValues = []
            for _ in range(0, 1024):
                si1 = bipolar.tick(input[i])
                si2 = bipolar.tick(input[i])
                si3 = bipolar.tick(input[i])
                si4 = bipolar.tick(input[i])
                si = si1 + si2 + si3 + si4
                m = 4
                bipolarValues.append(si)
                stream.append(2 * nStanh.tick(si, n * m) - 1)
                bipolarValues.append(si)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            s.append(probability)
            output.append(sum)

        # theorical model
        th_input = np.arange(-4.0, 4.1, 0.1)
        th_ouput = np.tanh(n * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker="o", label="NStanh(s)")
            plt.plot(th_input, th_ouput, color="orange", label="tanh(s)")
            plt.xlabel("s")
            plt.ylabel("ouput")
            plt.legend()
            plt.title("Approximation de NStanh avec m = 4 et offset = 8")
            plt.show()

    def NStanhTest4(self):
        """
        La valeur du offset est toujours (m * n) / 2
        n peut varier, mais par expérience, on obtient de bon résultats avec m = 4
        m = 8
        offset = 16
        n = 4
        """
        nStanh = NStanh(16)
        bipolar = B2ISBipolar()
        output = []
        input = np.arange(-128, 127, 1)
        s = []
        n = 4
        # practical model
        for i in range(0, len(input)):
            stream = []
            bipolarValues = []
            for _ in range(0, 1024):
                si1 = bipolar.tick(input[i])
                si2 = bipolar.tick(input[i])
                si3 = bipolar.tick(input[i])
                si4 = bipolar.tick(input[i])
                si5 = bipolar.tick(input[i])
                si6 = bipolar.tick(input[i])
                si7 = bipolar.tick(input[i])
                si8 = bipolar.tick(input[i])
                si = si1 + si2 + si3 + si4 + si5 + si6 + si7 + si8
                m = 8
                bipolarValues.append(si)
                stream.append(2 * nStanh.tick(si, n * m) - 1)
                bipolarValues.append(si)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            s.append(probability)
            output.append(sum)

        # theorical model
        th_input = np.arange(-8.0, 8.1, 0.1)
        th_ouput = np.tanh(n * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker="o", label="NStanh(s)")
            plt.plot(th_input, th_ouput, color="orange", label="tanh(s)")
            plt.xlabel("s")
            plt.ylabel("ouput")
            plt.legend()
            plt.title("Approximation de NStanh avec m = 8 et offset = 16")
            plt.show()

    def NStanhTest5(self):
        """
        La valeur du offset est toujours (m * n) / 2
        n peut varier, mais par expérience, on obtient de bon résultats avec m = 4
        m = 401
        offset = 128
        n = 4
        """
        nStanh = NStanh(256)
        bipolar = B2ISBipolar()
        output = []
        input = np.arange(-128, 128, 1)
        s = []
        n = 4
        # practical model
        for i in range(0, len(input)):
            stream = []
            bipolarValues = []
            for _ in range(0, 64):

                si = 0
                for _ in range(0, 401):
                    si += bipolar.tick(input[i])
                m = 401
                bipolarValues.append(si)
                stream.append(2 * nStanh.tick(si, n * m) - 1)
                bipolarValues.append(si)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            s.append(probability)
            output.append(sum)

        counter = 0
        for i in range(0, len(output)):
            if output[i] == 1:
                counter += 1
        print(f"counter of {counter}")

        # theorical model
        th_input = np.arange(-401.0, 401.0, 1.0)
        th_ouput = np.tanh(n * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker="o", label="NStanh(s)")
            plt.plot(th_input, th_ouput, color="orange", label="tanh(s)")
            plt.xlabel("s")
            plt.ylabel("ouput")
            plt.legend()
            plt.title("Approximation de NStanh avec m = 401 et offset = 256")
            plt.show()

    def NStanhTest6(self):
        """
        La valeur du offset est toujours (m * n) / 2
        n peut varier, mais par expérience, on obtient de bon résultats avec m = 4
        m = 8
        offset = 16
        n = 4
        """
        n = 16  # Tant que c'est un multiple de deux c'est correct
        m = 8
        nStanh = NStanh(
            (m * n) / 2
        )  # m * n / 2 (c'est le nombre d'états qu'il y a, le 0 est donc mi-chemin)
        bipolar = B2ISBipolar()
        output = []
        input = np.arange(-128, 127, 1)
        s = []
        # practical model
        for i in range(0, len(input)):
            stream = []
            bipolarValues = []
            for _ in range(0, 1024):
                si1 = bipolar.tick(input[i])
                si2 = bipolar.tick(input[i])
                si3 = bipolar.tick(input[i])
                si4 = bipolar.tick(input[i])
                si5 = bipolar.tick(input[i])
                si6 = bipolar.tick(input[i])
                si7 = bipolar.tick(input[i])
                si8 = bipolar.tick(input[i])
                si = si1 + si2 + si3 + si4 + si5 + si6 + si7 + si8
                bipolarValues.append(si)
                # Le 2 * nStanh - 1 permet d'avoir
                stream.append(2 * nStanh.tick(si, n * m) - 1)
                bipolarValues.append(si)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            s.append(probability)
            output.append(sum)

        # theorical model
        th_input = np.arange(-8.0, 8.1, 0.1)
        th_ouput = np.tanh(n * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker="o", label="NStanh(s)")
            plt.plot(th_input, th_ouput, color="orange", label="tanh(s)")
            plt.xlabel("s")
            plt.ylabel("ouput")
            plt.legend()
            plt.title("Approximation de NStanh avec m = 8 et offset = 16")
            plt.show()

    def NStanhTest7(self):
        """
        La valeur du offset est toujours (m * n) / 2
        n peut varier, mais par expérience, on obtient de bon résultats avec m = 4
        m = 401
        offset = 128
        n = 4
        """
        pixels = np.full(127, 128)
        weights = np.full(127, 0)
        n = 8
        m = 127
        nStanh = NStanh(offset=(n * m) / 2)
        bipolar = B2ISBipolar()
        unipolar = B2SUnipolar()
        bitwiseAnd = BitwiseOperatorAND()

        results = []
        for _ in range(0, 20):
            bitstream = []
            for _ in range(0, 1024):
                bpTot = 0
                up = unipolar.tick(128)
                for i in range(0, len(pixels)):
                    bpTot += bipolar.tick(0)

                andResult = bitwiseAnd.tick(bit=up, integralValue=bpTot)
                bitstream.append(andResult)

            Ex = np.average(bitstream)
            results.append(Ex)

        target_value = 0.25

        plt.figure(figsize=(10, 5))
        plt.plot(
            results, marker="o", linestyle="-", color="b", label="Values"
        )  # Line plot
        plt.title(
            "Variation des valeurs obtenus avec un neuron à 4 entrées. n = 8, m = 2. cycles = 1024",
            fontsize=14,
        )
        plt.xlabel("Tentative n#", fontsize=12)
        plt.ylabel("NStanh(s)", fontsize=12)
        plt.axhline(
            y=target_value,
            color="r",
            linestyle="--",
            label=f"Valeur théorique : {target_value}",
        )  # Horizontal line
        plt.grid(True, which="both", linestyle="--", linewidth=0.5)
        plt.legend()
        plt.show()

    def NStanhTest8(self):
        """
        La valeur du offset est toujours (m * n) / 2
        n peut varier, mais par expérience, on obtient de bon résultats avec m = 4
        m = 401
        offset = 128
        n = 8
        """
        n = 16
        m = 8
        nStanh = NStanh(offset=(n * m) / 2)
        bipolar = B2ISBipolar()

        input = np.arange(-128, 128, 1)

        results = []
        for i in input:
            nstanh_res = []
            for _ in range(0, 1024):
                bp = m * bipolar.tick(i)
                nstanh = nStanh.tick(Si=bp, mn=m * n)
                nstanh_res.append(nstanh)

            s = np.average(nstanh_res)
            results.append(s)

        # theoretical
        th_input = input
        th_output = (1 + np.tanh((n * th_input) / (256))) / 2
        th_input = th_input / 256

        enablePlot = True
        if enablePlot:
            plt.scatter(th_input, results, marker="o", label="NStanh(s)")
            plt.plot(th_input, th_output, color="orange", label=" (1 + tanh(s))/2")
            plt.xlabel("s")
            plt.ylabel("nstanh(s)")
            plt.legend()
            plt.title("Approximation de NStanh avec m = 8, n = 16")
            plt.show()

    def IntegrationTest1(self):
        print("### Integration Test")
        th_weight = [
            [-128, -128],
            [-64, -64],
            [-32, -32],
            [32, -32],
            [32, -64],
            [0, 0],
            [32, 32],
            [64, 64],
            [127, 127],
        ]
        th_weight = [[32, -32]]
        b2is1 = B2ISBipolar()
        pr_res = []
        print("### Practical")
        for j in range(0, len(th_weight)):
            stream2 = []
            stream1 = []
            stream_combined = []
            for _ in range(0, 20):
                val1 = b2is1.tick(th_weight[j][0])
                stream1.append(val1)

                val2 = b2is1.tick(th_weight[j][1])
                stream2.append(val2)

                val3 = val1 + val2
                stream_combined.append(val3)

            print(f"stream combined {stream_combined}")
            nStanh = NStanh(offset=2)
            m = 2
            result_after_stanh = []
            for i in range(0, len(stream_combined)):
                val = nStanh.tick(Si=stream_combined[i], mn=3 * m)
                result_after_stanh.append(val)
            print(f"result after stanh {result_after_stanh}")
            sum = 0
            for i in range(0, len(result_after_stanh)):
                sum = sum + result_after_stanh[i]

            stanh_average = sum / len(result_after_stanh)
            pr_res.append(stanh_average)
            print(f"weight {th_weight[j]} result {stanh_average}")

        print("### Theoretical")
        pixels = [255, 255]
        th_res = []
        for j in range(0, len(th_weight)):
            product = np.dot(th_weight[j], pixels)
            product = product / 65532
            res = (np.tanh(4 * product) + 1) / 2
            th_res.append(res)
            print(f"weight {th_weight[j]} result {res}")

        print("### Analyse")
        relative_difference = []
        for i in range(0, len(th_res)):
            abs_diff = abs(th_res[i] - pr_res[i])
            print(abs_diff)
            relative_difference.append(abs_diff * 100)

        average_difference = np.average(relative_difference)
        print(f"Average difference (%) : {average_difference}")

    def IntegrationTest2(self):
        print("### Integration Test 2")
        images = np.load("images.npy")
        y = np.load("practical.npy")

        correct = 0
        for j in range(0, 10):
            imgIndex = j
            pixels = images[imgIndex]

            results = np.zeros(10)
            neurons = [Neuron(i) for i in range(10)]
            for i in range(0, 10):
                neuron = neurons[i]
                counter = 0
                nbCycles = 1024
                for _ in range(0, nbCycles):
                    counter += neuron.tick(pixels)
                print(f"counter {counter}")
                probability = counter / nbCycles
                results[i] = probability
            print(f"results: {results}")
            prediction = results.argmax() + 1

            if prediction == y[imgIndex]:
                correct += 1
            print(f"Prediction {prediction} Label {y[imgIndex]}")

        print(f"percentage of correctness {correct / 100}")

    def NeuralNetworkPr1(self):
        print("### Neural Network Practical Value 1")
        image = [
            255,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            20,
            68,
            70,
            72,
            70,
            58,
            8,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            20,
            98,
            220,
            254,
            248,
            238,
            254,
            246,
            116,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            26,
            164,
            254,
            218,
            140,
            88,
            68,
            172,
            254,
            232,
            26,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            2,
            46,
            202,
            248,
            118,
            18,
            0,
            0,
            0,
            40,
            230,
            254,
            38,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            72,
            240,
            254,
            154,
            0,
            0,
            0,
            0,
            0,
            40,
            234,
            236,
            28,
            0,
            0,
            0,
            0,
            0,
            0,
            34,
            200,
            254,
            194,
            72,
            2,
            0,
            0,
            0,
            0,
            116,
            254,
            138,
            0,
            0,
            0,
            0,
            0,
            0,
            42,
            200,
            254,
            206,
            42,
            0,
            0,
            0,
            0,
            0,
            0,
            184,
            218,
            38,
            0,
            0,
            0,
            0,
            0,
            10,
            178,
            254,
            254,
            226,
            100,
            0,
            0,
            0,
            0,
            0,
            94,
            240,
            118,
            0,
            0,
            0,
            0,
            0,
            0,
            34,
            250,
            242,
            198,
            224,
            54,
            0,
            0,
            0,
            0,
            32,
            212,
            186,
            14,
            0,
            0,
            0,
            0,
            0,
            0,
            10,
            178,
            254,
            156,
            84,
            0,
            0,
            0,
            0,
            24,
            186,
            178,
            38,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            30,
            108,
            254,
            226,
            190,
            186,
            186,
            184,
            216,
            212,
            18,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            100,
            130,
            198,
            254,
            254,
            254,
            188,
            56,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            16,
            74,
            82,
            78,
            22,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
        ]
        pixels = image

        results = np.zeros(10)
        neurons = [Neuron(i) for i in range(10)]
        for i in range(0, 10):
            neuron = neurons[i]
            counter = 0
            nbCycles = 1024
            for _ in range(0, nbCycles):
                counter += neuron.tick(pixels)
            print(f"counter {counter}")
            probability = counter / nbCycles
            results[i] = probability
        print(f"results: {results}")
        prediction = results.argmax() + 1
        print(f"Prediction {prediction}")

    def NeuralNetworkTh1(self):
        print("### Neural Network Theoretical Values")
        # x = np.load("resources/x.npy")

        # x = np.load("images.npy")
        image = np.array(
            [
                255,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                20,
                68,
                70,
                72,
                70,
                58,
                8,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                20,
                98,
                220,
                254,
                248,
                238,
                254,
                246,
                116,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                26,
                164,
                254,
                218,
                140,
                88,
                68,
                172,
                254,
                232,
                26,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                2,
                46,
                202,
                248,
                118,
                18,
                0,
                0,
                0,
                40,
                230,
                254,
                38,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                72,
                240,
                254,
                154,
                0,
                0,
                0,
                0,
                0,
                40,
                234,
                236,
                28,
                0,
                0,
                0,
                0,
                0,
                0,
                34,
                200,
                254,
                194,
                72,
                2,
                0,
                0,
                0,
                0,
                116,
                254,
                138,
                0,
                0,
                0,
                0,
                0,
                0,
                42,
                200,
                254,
                206,
                42,
                0,
                0,
                0,
                0,
                0,
                0,
                184,
                218,
                38,
                0,
                0,
                0,
                0,
                0,
                10,
                178,
                254,
                254,
                226,
                100,
                0,
                0,
                0,
                0,
                0,
                94,
                240,
                118,
                0,
                0,
                0,
                0,
                0,
                0,
                34,
                250,
                242,
                198,
                224,
                54,
                0,
                0,
                0,
                0,
                32,
                212,
                186,
                14,
                0,
                0,
                0,
                0,
                0,
                0,
                10,
                178,
                254,
                156,
                84,
                0,
                0,
                0,
                0,
                24,
                186,
                178,
                38,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                30,
                108,
                254,
                226,
                190,
                186,
                186,
                184,
                216,
                212,
                18,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                100,
                130,
                198,
                254,
                254,
                254,
                188,
                56,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                16,
                74,
                82,
                78,
                22,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
            ]
        )

        weights = np.loadtxt("resources/weights.csv", delimiter=",").astype(np.int8)

        fpga_out = (
            np.dot(image.astype(np.int16), weights.T.astype(np.int16)) >> 8
        ).astype(np.int8)
        # Ça ne sert à rien d'appliquer la fonction NStanh
        pred = fpga_out.argmax() + 1

        print("FPGA Ouput: ", fpga_out)
        print("Prediction: ", pred)

    def TheoreticalValues(self):
        print("Theoretical Values")
        # todo, the values aren't pass in a tanh function
        # x = np.load("resources/x.npy")
        y = np.load("resources/y.npy")
        x = np.load("images.npy")
        practical = np.zeros_like(y)
        counter = 0
        for i in range(0, 5000):
            imageIndex = i
            weights = np.loadtxt("resources/weights.csv", delimiter=",").astype(np.int8)
            image = x[imageIndex]

            fpga_out = (
                np.dot(image.astype(np.int16), weights.T.astype(np.int16)) >> 8
            ).astype(np.int8)

            pred = fpga_out.argmax() + 1

            if pred == y[imageIndex]:
                counter += 1
            practical[i] = pred
            # print("FPGA Ouput: ", fpga_out)
            # print("Prediction: ", pred, "Label", y[imageIndex])
        # print(f"accuracy : {counter / 401}")
        np.save("practical.npy", practical)

    def IntegrationTestX(self):
        """
        La valeur du offset est toujours (m * n) / 2
        n peut varier, mais par expérience, on obtient de bon résultats avec m = 4
        m = 8
        offset = 16
        n = 4
        """
        n = 4  # Tant que c'est un multiple de deux c'est correct
        output = []
        input = np.arange(-128, 127, 1)

        s = []
        # practical model
        for i in range(0, len(input)):
            m = 8
            weights = np.full(m, input[i])
            pixels = np.zeros_like(weights) + 255
            neuron = Neuron(0, weights=weights, n=n, m=m)
            stream = []
            bipolarValues = []
            for _ in range(0, 1024):
                # Le 2 * nStanh - 1 permet d'avoir
                stream.append(2 * neuron.tick(pixels) - 1)
                bipolarValues.append(neuron.lastSi)

            sum = 0
            for j in range(0, len(stream)):
                sum = sum + stream[j]

            probability = 0
            for j in range(0, len(bipolarValues)):
                probability = probability + bipolarValues[j]

            sum = sum / len(stream)
            probability = probability / len(bipolarValues)
            s.append(probability)
            output.append(sum)

        # theorical model
        th_input = np.arange(-8.0, 8.1, 0.1)
        th_ouput = np.tanh(n * th_input / 2)

        enablePlot = True
        if enablePlot:
            plt.scatter(s, output, marker="o", label="NStanh(s)")
            plt.plot(th_input, th_ouput, color="orange", label="tanh(s)")
            plt.xlabel("s")
            plt.ylabel("ouput")
            plt.legend()
            plt.title("Approximation de NStanh avec m = 8 et offset = 16")
            plt.show()

    def IntegrationTestX2(self):
        n = 32  # Tant que c'est un multiple de deux c'est correct

        # practical model
        pixels = [128, 128, 128, 128, 128, 128, 128, 128]  # 0.5
        weights = [0, 0, 0, 0, 0, 0, 0, 0]  # 0.5
        m = len(pixels)
        neuron = Neuron(0, weights=weights, n=n, m=m)
        stream = []
        bipolarValues = []
        for _ in range(0, 1024):
            # Le 2 * nStanh - 1 permet d'avoir
            stream.append(neuron.tick(pixels))
            bipolarValues.append(neuron.lastSi)

        sum = 0
        for j in range(0, len(stream)):
            sum = sum + stream[j]

        sum = sum / len(stream)
        output = sum

        bipolarValues = np.average(bipolarValues)

        # theorical model
        s = (np.average(pixels) / 256) * ((127 + np.average(weights)) / 256)
        th_ouput = np.tanh(n * bipolarValues / 2)

        # Comparison
        print(f"Practical {(2 * output) - 1} Theorical {th_ouput}")
        print(f"Bipolar {bipolarValues}")


if __name__ == "__main__":
    test = Test()
    # test.B2ISTest0()
    # test.B2ISTest()
    # test.B2STest()
    # test.BitwiseANDTest()
    # test.UnipolarCounterTest()
    test.NStanhTest1()
    # test.NStanhTest2()
    # test.NStanhTest3()
    # test.NStanhTest4()
    # test.NStanhTest5()
    # test.NStanhTest6()
    # test.NStanhTest7()
    # test.NStanhTest8()
    # test.IntegrationTest1()
    # test.IntegrationTest2()
    # test.NeuralNetworkTh1()
    # test.NeuralNetworkPr1()
    # test.TheoreticalValues()
    # test.IntegrationTestX()
    # test.IntegrationTestX2()
