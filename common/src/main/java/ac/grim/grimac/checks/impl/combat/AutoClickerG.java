package ac.grim.grimac.checks.impl.combat;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

import java.util.*;

@CheckData(name = "AutoClicker (G)", setback = 0, description = "Analise de distribuicao temporal e bursts (Fork Concorrente Port)")
public class AutoclickerG extends Check implements PacketCheck {

    // Listas internas que substituem o EvictingQueue
    private final Deque<Long> intervals = new ArrayDeque<>();
    private final Deque<Integer> burstLengths = new ArrayDeque<>();
    private final Deque<Double> distributionScores = new ArrayDeque<>();

    private long lastSwingTime = -1L;
    private long lastAttackTime = 0L;
    private long burstStartTime = -1L;
    private int currentBurstCount = 0;
    private boolean isDigging = false;

    // Buffers de deteção
    private double burstBuffer = 0.0;
    private double distributionBuffer = 0.0;
    private double totalBuffer = 0.0;

    public AutoclickerG(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // 1. Rastreio de Mineração
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging dig = new WrapperPlayClientPlayerDigging(event);
            if (dig.getAction() == DiggingAction.START_DIGGING) isDigging = true;
            else if (dig.getAction() == DiggingAction.CANCELLED_DIGGING ||
                    dig.getAction() == DiggingAction.FINISHED_DIGGING) isDigging = false;
            return;
        }

        // 2. Rastreio de Ataque (Independente do core do Grim)
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                lastAttackTime = System.currentTimeMillis();
            }
        }

        // 3. Processamento de Cliques
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            WrapperPlayClientAnimation wrapper = new WrapperPlayClientAnimation(event);
            if (wrapper.getHand() != InteractionHand.MAIN_HAND || isDigging) return;

            // Ignora se não atacou recentemente (foco em combate)
            if (System.currentTimeMillis() - lastAttackTime > 500) return;

            long now = System.currentTimeMillis();
            if (lastSwingTime != -1L) {
                long interval = now - lastSwingTime;

                if (interval >= 20 && interval <= 1000) {
                    addInterval(interval);
                    analyzeBurst(interval, now);

                    if (intervals.size() >= 10) {
                        analyzeDistribution();
                    }
                }
            }
            lastSwingTime = now;

            // Quando a amostra está cheia (40 cliques), faz a análise final
            if (intervals.size() >= 40) {
                performFinalAnalysis();
                intervals.clear();
                burstLengths.clear();
                distributionScores.clear();
            }
        }
    }

    private void addInterval(long interval) {
        intervals.add(interval);
        if (intervals.size() > 40) intervals.removeFirst();
    }

    private void analyzeBurst(long interval, long currentTime) {
        if (interval <= 100) {
            if (currentBurstCount == 0) burstStartTime = currentTime;
            currentBurstCount++;
        } else {
            if (currentBurstCount >= 3) {
                burstLengths.add(currentBurstCount);
                if (burstLengths.size() > 20) burstLengths.removeFirst();
            }
            currentBurstCount = 0;
        }
    }

    private void analyzeDistribution() {
        double sum = 0;
        for (long val : intervals) sum += val;
        double mean = sum / intervals.size();

        // Teste de Wald-Wolfowitz para aleatoriedade
        double runs = 1;
        Double lastVal = null;
        for (long val : intervals) {
            double normalized = val / mean;
            if (lastVal != null) {
                if ((normalized > 1.0 && lastVal <= 1.0) || (normalized <= 1.0 && lastVal > 1.0)) {
                    runs++;
                }
            }
            lastVal = normalized;
        }

        int n = intervals.size();
        double expectedRuns = (2.0 * n - 1.0) / 3.0;
        double variance = (16.0 * n - 29.0) / 90.0;
        if (variance > 0) {
            double zScore = Math.abs((runs - expectedRuns) / Math.sqrt(variance));
            distributionScores.add(zScore);
            if (distributionScores.size() > 20) distributionScores.removeFirst();
        }
    }

    private void performFinalAnalysis() {
        // Analise de Bursts
        if (!burstLengths.isEmpty()) {
            double avgBurst = burstLengths.stream().mapToInt(i -> i).average().orElse(0);
            if (avgBurst > 5.0) burstBuffer += (avgBurst - 5.0) * 0.3;
        }
        burstBuffer = Math.max(0, burstBuffer - 0.1);

        // Analise de Z-Score (Deteção de "Falsa Bagunça")
        if (!distributionScores.isEmpty()) {
            double avgZ = distributionScores.stream().mapToDouble(d -> d).average().orElse(0);
            // Z-Score < 1.5 indica que a aleatoriedade é artificialmente estável
            if (avgZ < 1.5) distributionBuffer += (1.5 - avgZ) * 0.4;
        }
        distributionBuffer = Math.max(0, distributionBuffer - 0.1);

        totalBuffer = burstBuffer + distributionBuffer;

        if (totalBuffer > 6.0) {
            flagAndAlert(String.format("total=%.1f zScore=%.1f", totalBuffer, distributionBuffer));
            totalBuffer *= 0.6;
        }
    }
}
