package net;

import logic.Model;

public class Packet {
    private Model data;

    private Packet(Model model) {
        data = model;
    }

    public Model getData() {
        return data;
    }
}
