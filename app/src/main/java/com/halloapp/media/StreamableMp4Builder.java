package com.halloapp.media;

import com.halloapp.util.logs.Log;

import org.mp4parser.ParsableBox;
import org.mp4parser.boxes.iso14496.part12.EditBox;
import org.mp4parser.boxes.iso14496.part12.EditListBox;
import org.mp4parser.muxer.Edit;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;

import java.util.ArrayList;
import java.util.List;

public class StreamableMp4Builder extends DefaultMp4Builder {
    protected ParsableBox createEdts(Track track, Movie movie) {
        if (track.getEdits() != null && track.getEdits().size() > 0) {
            long movieTimeScale = getTimescale(movie);
            EditListBox elst = new EditListBox();
            elst.setVersion(0);
            List<EditListBox.Entry> entries = new ArrayList<EditListBox.Entry>();

            for (Edit edit : track.getEdits()) {
                entries.add(new EditListBox.Entry(elst,
                        Math.round(edit.getSegmentDuration() * movieTimeScale),
                        edit.getMediaTime() * track.getTrackMetaData().getTimescale() / edit.getTimeScale(),
                        edit.getMediaRate()));
            }

            elst.setEntries(entries);
            EditBox edts = new EditBox();
            edts.addBox(elst);
            return edts;
        } else {
            return null;
        }
    }
}
