package me.xuan.bdocr.sdk.model;


import java.util.List;

/**
 * Author: xuan
 * Created on 2019/10/23 14:53.
 * <p>
 * Describe:
 */
public class Word extends WordSimple {
    private Location location = new Location();
    private List<VertexesLocation> vertexesLocation;
    private List<Word.Char> characterResults;

    public Word() {
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<VertexesLocation> getVertexesLocations() {
        return this.vertexesLocation;
    }

    public void setVertexesLocations(List<VertexesLocation> vertexesLocation) {
        this.vertexesLocation = vertexesLocation;
    }

    public List<Word.Char> getCharacterResults() {
        return this.characterResults;
    }

    public void setCharacterResults(List<Word.Char> characterResults) {
        this.characterResults = characterResults;
    }

    public static class Char {
        private Location location = new Location();
        private String character;

        public Char() {
        }

        public String getCharacter() {
            return this.character;
        }

        public void setCharacter(String character) {
            this.character = character;
        }

        public Location getLocation() {
            return this.location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }
    }
}