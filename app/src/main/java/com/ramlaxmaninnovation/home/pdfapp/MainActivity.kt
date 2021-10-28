package  com.ramlaxmaninnovation.home.pdfapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ramlaxmaninnovation.home.pdfapp.TemplatePDF
import com.ramlaxmaninnovation.mds.R

class MainActivity : AppCompatActivity() {


    lateinit var templatePDF: TemplatePDF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }




    fun pdfView(view: View){


        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
            )
        }


        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
            )
        }

        templatePDF = TemplatePDF(applicationContext)
        templatePDF.openDocument()
        templatePDF.addMetaData("clientes", "Ventas", "Marines")
        templatePDF.addTitles("Teorema-Sistemas", "Comprovante de venda", "Rafael Reynoud Benetti", "R$ 60,00")

        templatePDF.addParagraph("Manteiga")
        templatePDF.addParagraph("R$ 50,00     x1 R$ 50,00")

        templatePDF.addParagraph("Produto_1")
        templatePDF.addParagraph("R$ 10,00     x1 R$ 10,00")

        templatePDF.addParagraph("")
        templatePDF.addParagraph("Sub total    x1 R$ 60,00")
        templatePDF.addParagraph("Desconto     x1 R$  0,00")
        templatePDF.addParagraph("valor total  x1 R$ 60,00")
        templatePDF.addParagraph("")

        templatePDF.addParagraph("Vendedor            Jose")
        templatePDF.addParagraph("Data                10/10/2018")


        //templatePDF.createTable(header, null)

        templatePDF.closeDocument()



        if(isExternalStorageWritable()){
            templatePDF.viewPDF(this)

        }else{
            println("asdfasdfasdf")
        }

    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

}
