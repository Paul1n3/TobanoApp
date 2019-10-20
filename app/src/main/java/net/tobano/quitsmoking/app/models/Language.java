package net.tobano.quitsmoking.app.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Language {
    public Long idLanguage;
    public String name;
    public String nameAbbrev;

    public Language() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Language(Long idLanguage, String name, String nameAbbrev) {
        this.idLanguage = idLanguage;
        this.name = name;
        this.nameAbbrev = nameAbbrev;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("idLanguage", idLanguage);
        result.put("name", name);
        result.put("nameAbbrev", nameAbbrev);

        return result;
    }
    // [END post_to_map]

    @Override
    public String toString() {
        return name;
    }

}
// [END post_class]
