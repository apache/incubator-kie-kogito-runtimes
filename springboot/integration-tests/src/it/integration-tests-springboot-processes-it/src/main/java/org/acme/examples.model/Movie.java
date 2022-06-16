/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.examples.model;

public class Movie {

    private String name;
    private int releaseYear;
    private Rating rating;
    private MovieGenre genre;

    public String getName() {
        return name;
    }

    public Movie setName(String name) {
        this.name = name;
        return this;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public Movie setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
        return this;
    }

    public Rating getRating() {
        return rating;
    }

    public Movie setRating(Rating rating) {
        this.rating = rating;
        return this;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public Movie setGenre(MovieGenre genre) {
        this.genre = genre;
        return this;
    }
}
