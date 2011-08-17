package com.c2c.controller;

import java.io.File;
import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mapfish.print.servlet.MapPrinterServlet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.lowagie.text.DocumentException;

/**
 * The Controller for handling compute requests.
 * <p/>
 * This class is registered as a bean in ws-servlet.xml.
 *
 * @author jeichar, pmauduit
 */
@Controller
@RequestMapping("/getreport")
public class GetReport extends AbstractQueryingController {

    private static class Printer extends MapPrinterServlet {
        @Override
        public void deleteFile(File file) {
            super.deleteFile(file);
        }

//        @Override
//        public void sendPdfFile(HttpServletResponse httpServletResponse, File tempFile, boolean inline) throws IOException {
//            super.sendPdfFile(httpServletResponse, tempFile, inline);
//        }

        @Override
        public void error(HttpServletResponse httpServletResponse, Throwable e) {
            super.error(httpServletResponse, e);
        }

        @Override
        public TempFile doCreatePDFFile(String spec, HttpServletRequest httpServletRequest) throws IOException, DocumentException, ServletException {
            return super.doCreatePDFFile(spec, httpServletRequest);
        }
    }
    
    
    private Printer printer;
    
    public synchronized Printer getPrinter() throws ServletException {
        if(printer == null) {
            printer = new Printer();
            printer.init();
        }
        return printer;
    }
    
    @PreDestroy
    public synchronized void destroy() {
        if(printer != null) {
            printer.destroy();
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public void getReport(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestParam("QUERYID") String queryId,
                       @RequestParam("BBOX") String bbox,
                       @RequestParam("WIDTH") int width,
                       @RequestParam("HEIGHT") int height,
                       @RequestParam("LAYOUT") String layout,
                       @RequestParam(value = "STYLEID", required = false) String styleId,
                       @RequestParam(value = "SRS", required = false) String srs)
            throws Exception {

        // TODO check that layout is an acceptable layout
        response.setContentType("application/pdf");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Expires", "-1");

        File tempFile = null;
        try {
            final String layoutTemplate = getTemplate(layout, request);
            String spec = String.format(layoutTemplate,queryId,bbox,width,height,layout,styleId,srs);
            tempFile = getPrinter().doCreatePDFFile(spec,request);
//            getPrinter().sendPdfFile(response, tempFile, true);
        } catch (Throwable e) {
            getPrinter().error(response, e);
        } finally {
            getPrinter().deleteFile(tempFile);
        }



    }

    private String getTemplate(String layout, HttpServletRequest request) {
        // TODO load json template for the layout.
        
        return null;
    }

}
