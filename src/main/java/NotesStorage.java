
import burp.api.montoya.persistence.Persistence;

import java.util.ArrayList;
import java.util.List;

import java.util.Collections;

public class NotesStorage {
    private static final String STORAGE_KEY = "saved_notes";
    private final Persistence persistence;
    private final List<Notes> notes;

    public NotesStorage(Persistence persistence) {
        this.persistence = persistence;
        this.notes = loadNotes();
    }

    public List<Notes> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    public void addNotes(Notes note) {
        if (!notes.contains(note)) {
            notes.add(note);
            saveNotes();
        }
    }

    public void deleteNotes(int index) {
        if (index >= 0 && index < notes.size()) {
            notes.remove(index);
            saveNotes();
        }
    }

    public void updateNotes(int index, Notes newNote) {
        if (index >= 0 && index < notes.size()) {
            notes.set(index, newNote);
            saveNotes();
        }
    }

    private void saveNotes() {
        String serializedNotes = String.join("\n", notes.stream().map(Notes::notes).toList());
        persistence.preferences().setString(STORAGE_KEY, serializedNotes);
    }

    private List<Notes> loadNotes() {
        String savedData = persistence.preferences().getString(STORAGE_KEY);
        if (savedData != null && !savedData.isEmpty()) {
            List<Notes> loadedNotes = new ArrayList<>();
            for (String noteText : savedData.split("\n")) {
                loadedNotes.add(new Notes(noteText));
            }
            return loadedNotes;
        }
        return new ArrayList<>();
    }
}
