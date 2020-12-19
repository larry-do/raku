package com.nyat.raku.dao.impl;

import com.nyat.raku.dao.TrackDAO;
import com.nyat.raku.entity.Track;
import com.nyat.raku.model.GenreDTO;
import com.nyat.raku.model.TrackDTO;
import com.nyat.raku.model.UserDTO;
import com.nyat.raku.payload.SearchPayload;
import com.nyat.raku.payload.SearchResult;
import javafx.util.Pair;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TrackDAOImpl implements TrackDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Track create(Track track) {
        entityManager.persist(track);
        return track;
    }

    @Override
    public Track get(Integer id) {
        return entityManager.find(Track.class, id);
    }

    @Override
    public List<Track> getAll() {
        return entityManager.createQuery("select t from Track t", Track.class).getResultList();
    }

    @Override
    public Track update(Track track) {
        entityManager.merge(track);
        return track;
    }

    @Override
    public void delete(Track track) {
        entityManager.remove(track);
    }

    @Override
    public Track getByCode(String username, String code) {
        try {
            return entityManager.createQuery("select t from Track t where t.code='" + code + "' and t.uploader.username = '" + username + "'", Track.class).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public SearchResult search(SearchPayload searchPayload) {
        SearchResult searchResult = new SearchResult();
        if (searchPayload.getKey().equals("track")) {
            List<Track> tracks = entityManager.createQuery("select t from Track t where lower(t.title) like lower('%" + searchPayload.getValue() + "%') or lower(t.code) like lower('%" + searchPayload.getValue() + "%')", Track.class)
                    .setFirstResult(searchPayload.getStart()).setMaxResults(searchPayload.getLength())
                    .getResultList();
            tracks.forEach(t -> {
                TrackDTO track = new TrackDTO();
                track.setId(t.getId());
                track.setTitle(t.getTitle());
                track.setArtist(t.getArtist());
                track.setDuration(t.getDuration());
                track.setTags(t.getTags());
                track.setGenres(t.getGenres().stream().map(genre -> {
                    GenreDTO genreDTO = new GenreDTO();
                    genreDTO.setId(genre.getId());
                    genreDTO.setCode(genre.getCode());
                    genreDTO.setName(genre.getName());
                    return genreDTO;
                }).collect(Collectors.toSet()));
                track.setDescription(t.getDescription());
                track.setPrivacy(t.getPrivacy());
                track.setPlays(t.getPlays());
                track.setExt(t.getExt());
                track.setUploadTime(t.getUploadTime());
                if (t.getImageUrl() != null) {
                    track.setImageUrl(t.getImageUrl());
                }
                track.setComposer(t.getComposer());
                track.setCode(t.getCode());
                UserDTO u = new UserDTO();
                u.setName(t.getUploader().getName());
                u.setUsername(t.getUploader().getUsername());
                u.setImageUrl(t.getUploader().getImageUrl());
                track.setUploader(u);
                searchResult.getResults().add(new Pair<>(track, "track"));
            });
        }
        return searchResult;
    }
}
