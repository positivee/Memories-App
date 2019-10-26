package pl.kamil_biernacki.memories;

public class MemoryModel {

    public String title,content,memoryTime;


    public MemoryModel(){

    }

    public MemoryModel(String title, String content, String memoryTime) {

        this.title = title;
        this.content = content;
        this.memoryTime = memoryTime;
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
