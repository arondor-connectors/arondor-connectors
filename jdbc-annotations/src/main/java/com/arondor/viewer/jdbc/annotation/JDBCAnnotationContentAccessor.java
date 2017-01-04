package com.arondor.viewer.jdbc.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import com.arondor.viewer.annotation.api.Annotation;
import com.arondor.viewer.annotation.exceptions.AnnotationsNotSupportedException;
import com.arondor.viewer.annotation.exceptions.InvalidAnnotationFormatException;
import com.arondor.viewer.client.api.document.DocumentId;
import com.arondor.viewer.xfdf.annotation.SerializedAnnotationContent;
import com.arondor.viewer.xfdf.annotation.SerializedAnnotationContentAccessor;

public class JDBCAnnotationContentAccessor implements SerializedAnnotationContentAccessor
{
    private DataSource dataSource;

    private String tableName = "ANNOTATIONS";

    private boolean caseSensitive = true;

    private DocumentId documentId;

    private boolean reverseDocumentId = false;

    private String useDocumentIdArgument = "documentId";

    public JDBCAnnotationContentAccessor()
    {
    }

    @Override
    public Collection<SerializedAnnotationContent> getAll(DocumentId documentId)
            throws AnnotationsNotSupportedException, InvalidAnnotationFormatException
    {
        List<SerializedAnnotationContent> contents = new ArrayList<SerializedAnnotationContent>();
        contents.add(getForModification(documentId, null));
        return contents;
    }

    @Override
    public SerializedAnnotationContent getForModification(DocumentId documentId, Annotation annotationToModify)
            throws AnnotationsNotSupportedException, InvalidAnnotationFormatException
    {
        return createContent(documentId, annotationToModify);
    }

    protected JDBCAnnotationContent createContent(DocumentId documentId, Annotation annotationToModify)
    {
        JDBCAnnotationContent content = new JDBCAnnotationContent(documentId);
        content.setDataSource(getDataSource());
        content.setCaseSensitive(isCaseSensitive());
        content.setTableName(getTableName());
        content.setReverseDocumentId(isReverseDocumentId());
        content.setUseDocumentIdArgument(getUseDocumentIdArgument());
        return content;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }

    public DocumentId getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(DocumentId documentId)
    {
        this.documentId = documentId;
    }

    public boolean isReverseDocumentId()
    {
        return reverseDocumentId;
    }

    public void setReverseDocumentId(boolean reverseDocumentId)
    {
        this.reverseDocumentId = reverseDocumentId;
    }

    public String getUseDocumentIdArgument()
    {
        return useDocumentIdArgument;
    }

    public void setUseDocumentIdArgument(String useDocumentIdArgument)
    {
        this.useDocumentIdArgument = useDocumentIdArgument;
    }
}