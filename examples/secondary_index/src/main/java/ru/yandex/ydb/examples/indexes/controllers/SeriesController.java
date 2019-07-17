package ru.yandex.ydb.examples.indexes.controllers;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ru.yandex.ydb.examples.indexes.model.Series;
import ru.yandex.ydb.examples.indexes.repositories.SeriesRepository;

@RestController
@RequestMapping("/series")
public class SeriesController {
    private static final Logger logger = LoggerFactory.getLogger(SeriesController.class);

    private final SeriesRepository seriesRepository;

    public SeriesController(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    @RequestMapping(path = "/drop_tables", method = RequestMethod.POST)
    @ResponseBody
    public String dropTables() {
        seriesRepository.dropTables();
        return "OK";
    }

    @RequestMapping(path = "/create_tables", method = RequestMethod.POST)
    @ResponseBody
    public String createTables() {
        seriesRepository.createTables();
        return "OK";
    }

    @RequestMapping(path = "/generate_random", method = RequestMethod.POST)
    public String generateRandom(
            @RequestParam long startId,
            @RequestParam int count,
            @RequestParam(defaultValue = "100") int maxConcurrency)
    {
        long nextId = startId;
        Deque<CompletableFuture<Void>> futures = new ArrayDeque<>();
        while (count > 0) {
            while (futures.size() >= maxConcurrency) {
                futures.getFirst().join();
                futures.removeFirst();
            }
            long seriesId = nextId++;
            logger.info("Creating series {}...", seriesId);
            futures.add(seriesRepository.insertAsync(
                    new Series(
                            seriesId,
                            "Name " + seriesId,
                            "Info " + seriesId,
                            LocalDate.now(),
                            ThreadLocalRandom.current().nextLong(0, 1000000))));
            --count;
        }
        while (!futures.isEmpty()) {
            futures.getFirst().join();
            futures.removeFirst();
        }
        return "OK";
    }

    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public List<Series> list(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long lastSeriesId)
    {
        List<Series> result;
        if (lastSeriesId != null) {
            result = seriesRepository.findAll(limit, lastSeriesId);
        } else {
            result = seriesRepository.findAll(limit);
        }
        return result;
    }

    @RequestMapping(path = "/most_viewed", method = RequestMethod.GET)
    public List<Series> mostViewed(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long lastSeriesId,
            @RequestParam(required = false) Long lastViews)
    {
        List<Series> result;
        if (lastSeriesId != null && lastViews != null) {
            result = seriesRepository.findMostViewed(limit, lastSeriesId, lastViews);
        } else {
            result = seriesRepository.findMostViewed(limit);
        }
        return result;
    }

    @RequestMapping(path = "/insert", method = RequestMethod.POST)
    public String insert(@RequestBody Series series) {
        seriesRepository.insert(series);
        return "OK";
    }

    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String delete(@PathVariable long id) {
        return "" + seriesRepository.delete(id);
    }

    @RequestMapping(path = "/update_views/{id}/{views}", method = RequestMethod.POST)
    public String updateViews(@PathVariable long id, @PathVariable long views) {
        return "" + seriesRepository.updateViews(id, views);
    }
}
