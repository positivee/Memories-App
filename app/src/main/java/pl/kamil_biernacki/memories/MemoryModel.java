package pl.kamil_biernacki.memories;

public class MemoryModel {

    public String title, content, memoryTime;
    public String image;


    public MemoryModel() {

    }

    public MemoryModel(String title, String content, String image, String memoryTime) {

        this.title = title;
        this.content = content;
        this.image = image;
        this.memoryTime = memoryTime;

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMemoryTime() {
        return memoryTime;
    }

    public void setMemoryTime(String memoryTime) {
        this.memoryTime = memoryTime;
    }
}
