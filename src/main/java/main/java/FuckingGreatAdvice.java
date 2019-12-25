package main.java;


import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;


public class FuckingGreatAdvice extends AnAction {

    private Integer httpConnectTimeout = 5000;
    private Integer httpSocketTimeout = 30 * 1000;

    private CloseableHttpClient buildCloseableHttpClient() {

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(httpConnectTimeout)
                .setSocketTimeout(httpSocketTimeout)
                .build();

        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

        return closeableHttpClient;
    }

    public String getAdvice() {

        HttpGet httpPost = new HttpGet("http://fucking-great-advice.ru/api/random");

        try (CloseableHttpClient client = this.buildCloseableHttpClient()) {

            try (CloseableHttpResponse response = client.execute(httpPost)) {

                String result = EntityUtils.toString(response.getEntity());

//                System.out.println(result);

                Integer index = result.indexOf("text");

                result = result.substring(index + 7);

//                System.out.println(result);

                index = result.indexOf("\"");

                result = result.substring(0, index);

//                System.out.println(result);

                return result;
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            return "Не получилось получить совет =(";
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        DataContext dataContext = DataManager.getInstance().getDataContext();
        Project project = (Project) dataContext.getData(DataConstants.PROJECT);

        IdeFrame ideFrame = WindowManager.getInstance().getIdeFrame(project);

        ExtCollector.showMessage(ideFrame, getAdvice(), MessageType.WARNING);
    }
}
