package com.example.messanger.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Invites implements Serializable {
    private List<Integer> invites;

    public Invites() {
        this.invites = new ArrayList<>();
        this.invites.add(-1);
    }

    public List<Integer> getInvites() {
        return invites;
    }

    public void setInvites(List<Integer> invites) {
        this.invites = invites;
    }
}
