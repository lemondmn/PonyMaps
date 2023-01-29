package mx.edu.ubicatec.ponymaps.models.Classes

class DataCard (title_: String, detail_: String, categoria_: String, image_: Int) {
    private var title = title_
    private var detail = detail_
    private var categoria = categoria_
    private var image = image_

    fun getTitle(): String {
        return title
    }
    fun getDetail(): String {
        return detail
    }
    fun getCategoria(): String {
        return categoria
    }
    fun getImage(): Int {
        return image
    }
}