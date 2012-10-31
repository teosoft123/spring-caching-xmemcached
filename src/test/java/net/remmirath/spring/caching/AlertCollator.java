package net.remmirath.spring.caching;

public class AlertCollator implements AlertListener {

    public volatile long alertCount = 0;

    @Override
    public void alert() {
        alertCount++;
    }

}
