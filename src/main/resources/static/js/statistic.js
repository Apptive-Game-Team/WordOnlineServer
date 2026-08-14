// Charts for the admin frame timing page. All data is embedded in the page by the server, so this
// script never makes a request and never needs a token. If Chart.js failed to load the tables
// still carry every number, so bail out quietly rather than throwing.
(function () {
    'use strict';

    var NS_PER_MS = 1000000;

    function readJson(id) {
        var el = document.getElementById(id);
        if (!el) {
            return [];
        }
        try {
            return JSON.parse(el.textContent) || [];
        } catch (e) {
            return [];
        }
    }

    function toMs(ns) {
        return ns === null || ns === undefined ? null : ns / NS_PER_MS;
    }

    if (typeof Chart === 'undefined') {
        return;
    }

    var timings = readJson('timingData');
    var timingCanvas = document.getElementById('timingChart');
    if (timingCanvas && timings.length) {
        new Chart(timingCanvas, {
            type: 'bar',
            data: {
                labels: timings.map(function (t) { return t.name; }),
                datasets: [
                    {
                        label: 'median of per-game means (ms)',
                        data: timings.map(function (t) { return toMs(t.medianMeanIntervalNs); }),
                        backgroundColor: '#4c78a8'
                    },
                    {
                        label: 'p95 of per-game means (ms)',
                        data: timings.map(function (t) { return toMs(t.p95MeanIntervalNs); }),
                        backgroundColor: '#f58518'
                    }
                ]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                scales: {
                    x: {
                        title: { display: true, text: 'milliseconds' },
                        beginAtZero: true
                    }
                }
            }
        });
    }

    var series = readJson('seriesData');
    var seriesCanvas = document.getElementById('seriesChart');
    if (seriesCanvas && series.length) {
        new Chart(seriesCanvas, {
            type: 'line',
            data: {
                labels: series.map(function (p) { return p.createdAt; }),
                datasets: [{
                    label: 'mean interval (ms)',
                    data: series.map(function (p) { return toMs(p.meanIntervalNs); }),
                    borderColor: '#4c78a8',
                    backgroundColor: '#4c78a8',
                    pointRadius: 2,
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        title: { display: true, text: 'milliseconds' },
                        beginAtZero: true
                    }
                }
            }
        });
    }
})();
