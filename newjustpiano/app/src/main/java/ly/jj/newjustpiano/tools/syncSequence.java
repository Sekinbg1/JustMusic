package ly.jj.newjustpiano.tools;

import ly.jj.newjustpiano.items.BarrageKey;

import static java.lang.Thread.sleep;
import static ly.jj.newjustpiano.items.StaticItems.BackgroundTimeDiv;
import static ly.jj.newjustpiano.items.StaticItems.isBackground;

public class syncSequence extends Sequence {
    private int sample;

    @Override
    public void sequence() {
        super.sequence();
        BarrageKey endKey = new BarrageKey(0, 0, 0, 0);
        for (Track track : tracks) {
            if (endKey.time < track.keys.get(track.keys.size() - 1).time) {
                endKey = track.keys.get(track.keys.size() - 1);
            }
        }
        BarrageKey lastKey = new BarrageKey(0, 0, 0, 0);
        BarrageKey nextKey = endKey;
        Track nextTrack = null;
        try {
            while (true) {
                for (Track track : tracks) {
                    if (track.get().time < nextKey.time) {
                        nextKey = track.get();
                        nextTrack = track;
                    }
                }
                nextTrack.play();
                long sleep_tick = nextKey.time - lastKey.time;
                long sleep_ms = sample * sleep_tick / 1000 / tick;
                int sleep_ns = (int) (sample * sleep_tick / tick % 1000) * 1000;
                if (sleep_ms < 0 || sleep_ns < 0) continue;
                sleep((long) (isBackground() ? sleep_ms * BackgroundTimeDiv : sleep_ms), sleep_ns);
                if (nextKey.value < 0x7f) {
                    onKey(nextKey.value, nextKey.volume);
                } else {
                    switch (nextKey.value & 0x7f) {
                        case 0x51:
                            sample = nextKey.volume;
                            break;
                        case 0x58:
                        case 0x59:
                            break;
                        case 0x2f:
                            if (nextTrack.isEnd())
                                tracks.remove(nextTrack);
                            if (tracks.isEmpty()) {
                                System.out.println("end sequencing");
                                return;
                            }
                    }
                }
                lastKey = nextKey;
                nextKey = endKey;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("end sequencing");
    }
}
