module NeuralNetworkWrapper(
(* mark_debug = "true" *) input        clock,
(* mark_debug = "true" *) input        reset,
(* mark_debug = "true" *) input  [7:0] s_axis_tdata,
(* mark_debug = "true" *) input        s_axis_tkeep,
(* mark_debug = "true" *) input        s_axis_tvalid,
(* mark_debug = "true" *) input        s_axis_tlast,
(* mark_debug = "true" *) output       s_axis_tready,
(* mark_debug = "true" *) output [7:0] m_axis_tdata,
(* mark_debug = "true" *) output       m_axis_tkeep,
(* mark_debug = "true" *) output       m_axis_tvalid,
(* mark_debug = "true" *) output       m_axis_tlast,
(* mark_debug = "true" *) input        m_axis_tready
);

NeuralNetwork inst(
    .clock(clock),
    .reset(!reset),
    .s_axis_tdata(s_axis_tdata),
    .s_axis_tkeep(s_axis_tkeep),
    .s_axis_tvalid(s_axis_tvalid),
    .s_axis_tlast(s_axis_tlast),
    .s_axis_tready(s_axis_tready),
    .m_axis_tdata(m_axis_tdata),
    .m_axis_tkeep(m_axis_tkeep),
    .m_axis_tvalid(m_axis_tvalid),
    .m_axis_tlast(m_axis_tlast),
    .m_axis_tready(m_axis_tready)
);

endmodule