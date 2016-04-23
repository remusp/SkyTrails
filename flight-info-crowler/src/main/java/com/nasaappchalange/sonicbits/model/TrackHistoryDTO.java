package com.nasaappchalange.sonicbits.model;

import java.util.List;

/**
 * @author dragos.nutu
 */
public class TrackHistoryDTO {
    private List<TrackInfoDTO> infos;

    public List<TrackInfoDTO> getInfos() {
        return infos;
    }

    public void setInfos(List<TrackInfoDTO> infos) {
        this.infos = infos;
    }
}
