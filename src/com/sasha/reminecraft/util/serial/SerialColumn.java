package com.sasha.reminecraft.util.serial;

import com.github.steveice10.mc.protocol.data.game.chunk.Column;

import java.io.Serializable;

public class SerialColumn implements Serializable {

    private transient Column column;
    private Long hash;

    public SerialColumn(Column column, Long hash) {
        this.column = column;
        this.hash = hash;
    }

    public Column getColumn() {
        return column;
    }

    public Long getHash() {
        return hash;
    }

    public void setColumn(Column column) {
        this.column = column;
    }
}
