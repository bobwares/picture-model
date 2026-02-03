/**
 * App: Picture Model
 * Package: com.picturemodel.service
 * File: ReactiveRepositoryWrapper.java
 * Version: 0.1.1
 * Turns: 16,15
 * Author: Bobwares (bobwares@outlook.com)
 * Date: 2026-02-03T04:55:30Z
 * Exports: ReactiveRepositoryWrapper
 * Description: Service to wrap blocking JPA repository calls with reactive types using boundedElastic scheduler.
 */

package com.picturemodel.service;

import com.picturemodel.domain.entity.*;
import com.picturemodel.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Reactive wrapper for all JPA repositories.
 * Isolates blocking database calls on boundedElastic scheduler.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveRepositoryWrapper {

    private final ImageRepository imageRepository;
    private final ImageMetadataRepository imageMetadataRepository;
    private final RemoteFileDriveRepository driveRepository;
    private final CrawlJobRepository crawlJobRepository;
    private final TagRepository tagRepository;

    // ==================== RemoteFileDrive Operations ====================

    public Mono<RemoteFileDrive> saveDrive(RemoteFileDrive drive) {
        return Mono.fromCallable(() -> driveRepository.save(drive))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<RemoteFileDrive> findAllDrives() {
        return Mono.fromCallable(driveRepository::findAll)
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<RemoteFileDrive> findDriveById(UUID id) {
        return Mono.fromCallable(() -> driveRepository.findById(id))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteDrive(UUID id) {
        return Mono.fromRunnable(() -> driveRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public Mono<Long> countDrives() {
        return Mono.fromCallable(driveRepository::count)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Long> countDrivesByStatus(com.picturemodel.domain.enums.ConnectionStatus status) {
        return Mono.fromCallable(() -> driveRepository.countByStatus(status))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Image Operations ====================

    public Mono<Image> saveImage(Image image) {
        return Mono.fromCallable(() -> imageRepository.save(image))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<Image> saveAllImages(List<Image> images) {
        return Mono.fromCallable(() -> imageRepository.saveAll(images))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Image> findImageById(UUID id) {
        return Mono.fromCallable(() -> imageRepository.findWithDriveById(id))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Image> findImageDetailById(UUID id) {
        return Mono.fromCallable(() -> imageRepository.findDetailById(id))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Image> findImageByDriveAndPath(UUID driveId, String filePath) {
        return Mono.fromCallable(() -> imageRepository.findByDrive_IdAndFilePath(driveId, filePath))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<Image>> findImagesByDrive(UUID driveId, Pageable pageable) {
        return Mono.fromCallable(() -> imageRepository.findByDrive_IdAndDeletedFalse(driveId, pageable))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<Image>> findImagesByDriveAndDirectory(UUID driveId, String dirPath, Pageable pageable) {
        return Mono.fromCallable(() -> imageRepository.findByDrive_IdAndDirectoryAndDeletedFalse(driveId, dirPath, pageable))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<Image>> findAllImagesNotDeleted(Pageable pageable) {
        return Mono.fromCallable(() -> imageRepository.findByDeletedFalse(pageable))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Long> countImagesByDrive(UUID driveId) {
        return Mono.fromCallable(() -> imageRepository.countByDrive_IdAndDeletedFalse(driveId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Long> countAllImages() {
        return Mono.fromCallable(imageRepository::count)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<Image> findAllImagesByDrive(UUID driveId) {
        return Mono.fromCallable(() -> imageRepository.findAllByDrive_Id(driveId))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<Image>> findImagesBySpecification(Specification<Image> spec, Pageable pageable) {
        return Mono.fromCallable(() -> imageRepository.findAll(spec, pageable))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== ImageMetadata Operations ====================

    public Mono<ImageMetadata> saveImageMetadata(ImageMetadata metadata) {
        return Mono.fromCallable(() -> imageMetadataRepository.save(metadata))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<ImageMetadata> findMetadataByImageId(UUID imageId) {
        return Mono.fromCallable(() -> imageMetadataRepository.findByImageId(imageId))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== CrawlJob Operations ====================

    public Mono<CrawlJob> saveCrawlJob(CrawlJob job) {
        return Mono.fromCallable(() -> crawlJobRepository.save(job))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<CrawlJob> findCrawlJobsByDrive(UUID driveId, Pageable pageable) {
        return Mono.fromCallable(() -> crawlJobRepository.findByDrive_IdOrderByStartTimeDesc(driveId, pageable))
                .flatMapMany(page -> Flux.fromIterable(page.getContent()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<CrawlJob> findRecentCrawlJobs(Pageable pageable) {
        return Mono.fromCallable(() -> crawlJobRepository.findAll(pageable))
                .flatMapMany(page -> Flux.fromIterable(page.getContent()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<CrawlJob> findCrawlJobById(UUID id) {
        return Mono.fromCallable(() -> crawlJobRepository.findById(id))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Long> countCrawlJobsByStatus(com.picturemodel.domain.enums.CrawlStatus status) {
        return Mono.fromCallable(() -> crawlJobRepository.countByStatus(status))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<CrawlJob>> findAllCrawlJobs(Pageable pageable) {
        return Mono.fromCallable(() -> crawlJobRepository.findAll(pageable))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<CrawlJob>> findCrawlJobsByDriveOrderByStartTime(UUID driveId, Pageable pageable) {
        return Mono.fromCallable(() -> crawlJobRepository.findByDrive_IdOrderByStartTimeDesc(driveId, pageable))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== Tag Operations ====================

    public Mono<Tag> saveTag(Tag tag) {
        return Mono.fromCallable(() -> tagRepository.save(tag))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<Tag> findAllTags() {
        return Mono.fromCallable(tagRepository::findAll)
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Tag> findTagById(UUID id) {
        return Mono.fromCallable(() -> tagRepository.findById(id))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Tag> findTagByName(String name) {
        return Mono.fromCallable(() -> tagRepository.findByName(name))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Tag> findTagByNameIgnoreCase(String name) {
        return Mono.fromCallable(() -> tagRepository.findByNameIgnoreCase(name))
                .flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<Tag> findAllTagsSorted() {
        return Mono.fromCallable(() -> tagRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.ASC, "name")))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Long> countAllTags() {
        return Mono.fromCallable(tagRepository::count)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteTag(Tag tag) {
        return Mono.fromRunnable(() -> tagRepository.delete(tag))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public Mono<Void> deleteTagById(UUID id) {
        return Mono.fromRunnable(() -> tagRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
