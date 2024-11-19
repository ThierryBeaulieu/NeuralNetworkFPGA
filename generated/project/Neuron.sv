// Generated by CIRCT firtool-1.62.0
module MaxPeriodFibonacciLFSR(
  input  clock,
         reset,
  output io_out_0,
         io_out_1,
         io_out_2,
         io_out_3,
         io_out_4,
         io_out_5,
         io_out_6,
         io_out_7
);

  reg state_0;
  reg state_1;
  reg state_2;
  reg state_3;
  reg state_4;
  reg state_5;
  reg state_6;
  reg state_7;
  always @(posedge clock) begin
    if (reset) begin
      state_0 <= 1'h0;
      state_1 <= 1'h1;
      state_2 <= 1'h0;
      state_3 <= 1'h0;
      state_4 <= 1'h0;
      state_5 <= 1'h1;
      state_6 <= 1'h0;
      state_7 <= 1'h0;
    end
    else begin
      state_0 <= state_7 ^ state_5 ^ state_4 ^ state_3;
      state_1 <= state_0;
      state_2 <= state_1;
      state_3 <= state_2;
      state_4 <= state_3;
      state_5 <= state_4;
      state_6 <= state_5;
      state_7 <= state_6;
    end
  end // always @(posedge)
  assign io_out_0 = state_0;
  assign io_out_1 = state_1;
  assign io_out_2 = state_2;
  assign io_out_3 = state_3;
  assign io_out_4 = state_4;
  assign io_out_5 = state_5;
  assign io_out_6 = state_6;
  assign io_out_7 = state_7;
endmodule

module B2SUnipolar(
  input        clock,
               reset,
  input  [7:0] io_inputPixel,
  output       io_outputStream
);

  wire _randomNumber_prng_io_out_0;
  wire _randomNumber_prng_io_out_1;
  wire _randomNumber_prng_io_out_2;
  wire _randomNumber_prng_io_out_3;
  wire _randomNumber_prng_io_out_4;
  wire _randomNumber_prng_io_out_5;
  wire _randomNumber_prng_io_out_6;
  wire _randomNumber_prng_io_out_7;
  MaxPeriodFibonacciLFSR randomNumber_prng (
    .clock    (clock),
    .reset    (reset),
    .io_out_0 (_randomNumber_prng_io_out_0),
    .io_out_1 (_randomNumber_prng_io_out_1),
    .io_out_2 (_randomNumber_prng_io_out_2),
    .io_out_3 (_randomNumber_prng_io_out_3),
    .io_out_4 (_randomNumber_prng_io_out_4),
    .io_out_5 (_randomNumber_prng_io_out_5),
    .io_out_6 (_randomNumber_prng_io_out_6),
    .io_out_7 (_randomNumber_prng_io_out_7)
  );
  assign io_outputStream =
    {_randomNumber_prng_io_out_7,
     _randomNumber_prng_io_out_6,
     _randomNumber_prng_io_out_5,
     _randomNumber_prng_io_out_4,
     _randomNumber_prng_io_out_3,
     _randomNumber_prng_io_out_2,
     _randomNumber_prng_io_out_1,
     _randomNumber_prng_io_out_0} < io_inputPixel;
endmodule

module B2ISBipolar(
  input        clock,
               reset,
  input  [7:0] io_inputWeight,
  output [1:0] io_outputStream
);

  wire _randomNumber_prng_io_out_0;
  wire _randomNumber_prng_io_out_1;
  wire _randomNumber_prng_io_out_2;
  wire _randomNumber_prng_io_out_3;
  wire _randomNumber_prng_io_out_4;
  wire _randomNumber_prng_io_out_5;
  wire _randomNumber_prng_io_out_6;
  wire _randomNumber_prng_io_out_7;
  MaxPeriodFibonacciLFSR randomNumber_prng (
    .clock    (clock),
    .reset    (reset),
    .io_out_0 (_randomNumber_prng_io_out_0),
    .io_out_1 (_randomNumber_prng_io_out_1),
    .io_out_2 (_randomNumber_prng_io_out_2),
    .io_out_3 (_randomNumber_prng_io_out_3),
    .io_out_4 (_randomNumber_prng_io_out_4),
    .io_out_5 (_randomNumber_prng_io_out_5),
    .io_out_6 (_randomNumber_prng_io_out_6),
    .io_out_7 (_randomNumber_prng_io_out_7)
  );
  assign io_outputStream =
    {$signed({_randomNumber_prng_io_out_7,
              _randomNumber_prng_io_out_6,
              _randomNumber_prng_io_out_5,
              _randomNumber_prng_io_out_4,
              _randomNumber_prng_io_out_3,
              _randomNumber_prng_io_out_2,
              _randomNumber_prng_io_out_1,
              _randomNumber_prng_io_out_0} - 8'h80) >= $signed(io_inputWeight),
     1'h1};
endmodule

module BitwiseAND(
  input  [1:0] io_inputInteger,
  input        io_inputBit,
  output [1:0] io_outputStream
);

  assign io_outputStream = io_inputBit ? io_inputInteger : 2'h0;
endmodule

module TreeAdder(
  input  [1:0] io_inputStream_0,
               io_inputStream_1,
               io_inputStream_2,
               io_inputStream_3,
  output [4:0] io_outputStream
);

  wire [2:0] _io_outputStream_T =
    {io_inputStream_0[1], io_inputStream_0} + {io_inputStream_1[1], io_inputStream_1};
  wire [2:0] _io_outputStream_T_1 =
    {io_inputStream_2[1], io_inputStream_2} + {io_inputStream_3[1], io_inputStream_3};
  wire [3:0] _io_outputStream_T_2 =
    {_io_outputStream_T[2], _io_outputStream_T}
    + {_io_outputStream_T_1[2], _io_outputStream_T_1};
  assign io_outputStream = {_io_outputStream_T_2[3], _io_outputStream_T_2};
endmodule

module NStanh(
  input        clock,
               reset,
  input  [4:0] io_inputSi,
  output       io_outputStream
);

  reg [9:0] m_counter;
  always @(posedge clock) begin
    if (reset)
      m_counter <= 10'h0;
    else if ($signed(m_counter) < 10'sh0)
      m_counter <= 10'h0;
    else if ($signed(m_counter) > 10'shF)
      m_counter <= 10'hF;
    else
      m_counter <= m_counter + {{5{io_inputSi[4]}}, io_inputSi};
  end // always @(posedge)
  assign io_outputStream = $signed(m_counter) > 10'sh8;
endmodule

module Neuron(
  input        clock,
               reset,
  input  [7:0] io_inputPixels_0,
               io_inputPixels_1,
               io_inputPixels_2,
               io_inputPixels_3,
               io_inputWeights_0,
               io_inputWeights_1,
               io_inputWeights_2,
               io_inputWeights_3,
  output       io_outputB2SValues_0,
               io_outputB2SValues_1,
               io_outputB2SValues_2,
               io_outputB2SValues_3,
  output [1:0] io_outputB2ISValues_0,
               io_outputB2ISValues_1,
               io_outputB2ISValues_2,
               io_outputB2ISValues_3,
               io_outputANDValues_0,
               io_outputANDValues_1,
               io_outputANDValues_2,
               io_outputANDValues_3,
  output [4:0] io_outputTreeAdder,
  output       io_outputStream
);

  wire [4:0] _treeAdder_io_outputStream;
  wire [1:0] _bitwiseAND_3_io_outputStream;
  wire [1:0] _bitwiseAND_2_io_outputStream;
  wire [1:0] _bitwiseAND_1_io_outputStream;
  wire [1:0] _bitwiseAND_0_io_outputStream;
  wire [1:0] _b2ISBipolar_3_io_outputStream;
  wire [1:0] _b2ISBipolar_2_io_outputStream;
  wire [1:0] _b2ISBipolar_1_io_outputStream;
  wire [1:0] _b2ISBipolar_0_io_outputStream;
  wire       _b2SUnipolar_3_io_outputStream;
  wire       _b2SUnipolar_2_io_outputStream;
  wire       _b2SUnipolar_1_io_outputStream;
  wire       _b2SUnipolar_0_io_outputStream;
  reg        regB2S_0;
  reg        regB2S_1;
  reg        regB2S_2;
  reg        regB2S_3;
  reg  [1:0] regB2IS_0;
  reg  [1:0] regB2IS_1;
  reg  [1:0] regB2IS_2;
  reg  [1:0] regB2IS_3;
  reg  [1:0] regAND_0;
  reg  [1:0] regAND_1;
  reg  [1:0] regAND_2;
  reg  [1:0] regAND_3;
  always @(posedge clock) begin
    if (reset) begin
      regB2S_0 <= 1'h0;
      regB2S_1 <= 1'h0;
      regB2S_2 <= 1'h0;
      regB2S_3 <= 1'h0;
      regB2IS_0 <= 2'h0;
      regB2IS_1 <= 2'h0;
      regB2IS_2 <= 2'h0;
      regB2IS_3 <= 2'h0;
      regAND_0 <= 2'h0;
      regAND_1 <= 2'h0;
      regAND_2 <= 2'h0;
      regAND_3 <= 2'h0;
    end
    else begin
      regB2S_0 <= _b2SUnipolar_0_io_outputStream;
      regB2S_1 <= _b2SUnipolar_1_io_outputStream;
      regB2S_2 <= _b2SUnipolar_2_io_outputStream;
      regB2S_3 <= _b2SUnipolar_3_io_outputStream;
      regB2IS_0 <= _b2ISBipolar_0_io_outputStream;
      regB2IS_1 <= _b2ISBipolar_1_io_outputStream;
      regB2IS_2 <= _b2ISBipolar_2_io_outputStream;
      regB2IS_3 <= _b2ISBipolar_3_io_outputStream;
      regAND_0 <= _bitwiseAND_0_io_outputStream;
      regAND_1 <= _bitwiseAND_1_io_outputStream;
      regAND_2 <= _bitwiseAND_2_io_outputStream;
      regAND_3 <= _bitwiseAND_3_io_outputStream;
    end
  end // always @(posedge)
  B2SUnipolar b2SUnipolar_0 (
    .clock           (clock),
    .reset           (reset),
    .io_inputPixel   (io_inputPixels_0),
    .io_outputStream (_b2SUnipolar_0_io_outputStream)
  );
  B2SUnipolar b2SUnipolar_1 (
    .clock           (clock),
    .reset           (reset),
    .io_inputPixel   (io_inputPixels_1),
    .io_outputStream (_b2SUnipolar_1_io_outputStream)
  );
  B2SUnipolar b2SUnipolar_2 (
    .clock           (clock),
    .reset           (reset),
    .io_inputPixel   (io_inputPixels_2),
    .io_outputStream (_b2SUnipolar_2_io_outputStream)
  );
  B2SUnipolar b2SUnipolar_3 (
    .clock           (clock),
    .reset           (reset),
    .io_inputPixel   (io_inputPixels_3),
    .io_outputStream (_b2SUnipolar_3_io_outputStream)
  );
  B2ISBipolar b2ISBipolar_0 (
    .clock           (clock),
    .reset           (reset),
    .io_inputWeight  (io_inputWeights_0),
    .io_outputStream (_b2ISBipolar_0_io_outputStream)
  );
  B2ISBipolar b2ISBipolar_1 (
    .clock           (clock),
    .reset           (reset),
    .io_inputWeight  (io_inputWeights_1),
    .io_outputStream (_b2ISBipolar_1_io_outputStream)
  );
  B2ISBipolar b2ISBipolar_2 (
    .clock           (clock),
    .reset           (reset),
    .io_inputWeight  (io_inputWeights_2),
    .io_outputStream (_b2ISBipolar_2_io_outputStream)
  );
  B2ISBipolar b2ISBipolar_3 (
    .clock           (clock),
    .reset           (reset),
    .io_inputWeight  (io_inputWeights_3),
    .io_outputStream (_b2ISBipolar_3_io_outputStream)
  );
  BitwiseAND bitwiseAND_0 (
    .io_inputInteger (regB2IS_0),
    .io_inputBit     (regB2S_0),
    .io_outputStream (_bitwiseAND_0_io_outputStream)
  );
  BitwiseAND bitwiseAND_1 (
    .io_inputInteger (regB2IS_1),
    .io_inputBit     (regB2S_1),
    .io_outputStream (_bitwiseAND_1_io_outputStream)
  );
  BitwiseAND bitwiseAND_2 (
    .io_inputInteger (regB2IS_2),
    .io_inputBit     (regB2S_2),
    .io_outputStream (_bitwiseAND_2_io_outputStream)
  );
  BitwiseAND bitwiseAND_3 (
    .io_inputInteger (regB2IS_3),
    .io_inputBit     (regB2S_3),
    .io_outputStream (_bitwiseAND_3_io_outputStream)
  );
  TreeAdder treeAdder (
    .io_inputStream_0 (regAND_0),
    .io_inputStream_1 (regAND_1),
    .io_inputStream_2 (regAND_2),
    .io_inputStream_3 (regAND_3),
    .io_outputStream  (_treeAdder_io_outputStream)
  );
  NStanh nStanh (
    .clock           (clock),
    .reset           (reset),
    .io_inputSi      (_treeAdder_io_outputStream),
    .io_outputStream (io_outputStream)
  );
  assign io_outputB2SValues_0 = regB2S_0;
  assign io_outputB2SValues_1 = regB2S_1;
  assign io_outputB2SValues_2 = regB2S_2;
  assign io_outputB2SValues_3 = regB2S_3;
  assign io_outputB2ISValues_0 = regB2IS_0;
  assign io_outputB2ISValues_1 = regB2IS_1;
  assign io_outputB2ISValues_2 = regB2IS_2;
  assign io_outputB2ISValues_3 = regB2IS_3;
  assign io_outputANDValues_0 = regAND_0;
  assign io_outputANDValues_1 = regAND_1;
  assign io_outputANDValues_2 = regAND_2;
  assign io_outputANDValues_3 = regAND_3;
  assign io_outputTreeAdder = _treeAdder_io_outputStream;
endmodule

