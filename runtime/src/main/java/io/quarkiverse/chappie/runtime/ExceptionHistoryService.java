package io.quarkiverse.chappie.runtime;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Keeps a history of the exception (picked op from the log file)
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@ApplicationScoped
public class ExceptionHistoryService {
    private final FixedSizeQueue<ExceptionDetail> lastFewExceptions = new FixedSizeQueue<>(5);

    @Inject
    private AIAssistant aiAssistant;

    public void addExceptionDetail(ExceptionDetail exceptionDetail) {
        this.lastFewExceptions.add(exceptionDetail);

        // TODO: Remove below: Just to check
        SuggestedFix fix = helpFixException(exceptionDetail);

        System.out.println("===========================================");
        System.out.println(fix.response());
        System.out.println("\n\n");
        System.out.println(fix.explanation());
        System.out.println("\n\n");
        System.out.println(fix.diff());
        System.out.println("\n\n");
        System.out.println(fix.suggestedSource());
    }

    public Optional<ExceptionDetail> getLatestExceptionDetails() {
        if (!this.lastFewExceptions.isEmpty()) {
            return Optional.of(this.lastFewExceptions.getLast());
        }
        return Optional.empty();
    }

    public List<ExceptionDetail> getExceptionHistory() {
        return this.lastFewExceptions;
    }

    public SuggestedFix helpFixException(ExceptionDetail exceptionDetail) {
        String source = "No source code";
        if (exceptionDetail.effectedSourceFilesContent() != null && !exceptionDetail.effectedSourceFilesContent().isEmpty()) {
            source = String.join("\n\n", exceptionDetail.effectedSourceFilesContent());
        }

        return this.aiAssistant.helpFix(exceptionDetail.stacktrace(), source);
    }

    class FixedSizeQueue<E> extends LinkedList<E> {
        private final int maxSize;

        public FixedSizeQueue(int maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public boolean add(E e) {
            if (size() >= maxSize) {
                removeFirst();
            }
            return super.add(e);
        }
    }
}
