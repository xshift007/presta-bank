package com.example.loanapplication.service;

import com.example.loanapplication.entity.LoanApplication;
import com.example.loanapplication.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Service
public class LoanApplicationService {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    private final Path fileStorageLocation;


    @Autowired
    public LoanApplicationService() {
        this.fileStorageLocation = Paths.get("uploads")
                .toAbsolutePath()
                .normalize();
        try {
            Files.createDirectories(this.fileStorageLocation.resolve("avaluos"));
            Files.createDirectories(this.fileStorageLocation.resolve("ingresos"));
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento de archivos.", ex);
        }
    }
    public LoanApplication createLoanApplication(String nombreCompleto, String tipoPrestamo, BigDecimal montoSolicitado,
                                                 Integer plazoSolicitado, BigDecimal tasaInteres, MultipartFile comprobanteAvaluo, MultipartFile comprobanteIngresos) {

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setNombreCompleto(nombreCompleto);
        loanApplication.setTipoPrestamo(tipoPrestamo);
        loanApplication.setMontoSolicitado(montoSolicitado);
        loanApplication.setPlazoSolicitado(plazoSolicitado);
        loanApplication.setTasaInteres(tasaInteres);
        loanApplication.setFechaSolicitud(LocalDateTime.now());
        loanApplication.setEstadoSolicitud("EN_REVISION_INICIAL");

        String avaluoPath = guardarArchivo(comprobanteAvaluo, "avaluos");
        String ingresosPath = guardarArchivo(comprobanteIngresos, "ingresos");
        loanApplication.setDocumentosAdjuntos(avaluoPath + "," + ingresosPath);
        return loanApplicationRepository.save(loanApplication);
    }

    private String guardarArchivo(MultipartFile archivo, String carpeta) {
        String nombreArchivo = StringUtils.cleanPath(archivo.getOriginalFilename());
        try {
            if (nombreArchivo.contains("..")) {
                throw new RuntimeException("Archivo inválido: " + nombreArchivo);
            }

            Path destinoRuta = this.fileStorageLocation.resolve(carpeta).resolve(nombreArchivo);
            Files.copy(archivo.getInputStream(), destinoRuta, StandardCopyOption.REPLACE_EXISTING);
            return destinoRuta.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Error al guardar el archivo " + nombreArchivo, ex);
        }
    }

    public LoanApplication getLoanApplicationById(Long id) {
        return loanApplicationRepository.findById(id).orElse(null);
    }
}