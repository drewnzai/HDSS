import { ChevronLeft, ChevronRight } from "lucide-react";
import type { ChangeEvent } from "react";

interface DataTablePaginationProps {
    page: number;
    pageSize: number;
    totalItems: number;

    onPageChange: (page: number) => void;
    onPageSizeChange?: (pageSize: number) => void;

    pageSizeOptions?: number[];
}

function DataTablePagination({
    page,
    pageSize,
    totalItems,
    onPageChange,
    onPageSizeChange,
    pageSizeOptions = [10, 25, 50],
}: DataTablePaginationProps) {
    const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));
    const hasPrevious = page > 0;
    const hasNext = page < totalPages - 1;

    const startItem =
        totalItems === 0 ? 0 : page * pageSize + 1;

    const endItem =
        totalItems === 0
            ? 0
            : Math.min((page + 1) * pageSize, totalItems);

    const handlePageSizeChange = (
        event: ChangeEvent<HTMLSelectElement>
    ) => {
        const nextPageSize = Number(event.target.value);

        onPageSizeChange?.(nextPageSize);
        onPageChange(0);
    };

    return (
        <div className="data-table-pagination">
            <div className="data-table-pagination__summary">
                Showing{" "}
                <strong>
                    {startItem}–{endItem}
                </strong>{" "}
                of <strong>{totalItems}</strong>
            </div>

            <div className="data-table-pagination__controls">
                {onPageSizeChange && (
                    <label className="data-table-pagination__size">
                        <span>Rows</span>

                        <select
                            value={pageSize}
                            onChange={handlePageSizeChange}
                            aria-label="Rows per page"
                        >
                            {pageSizeOptions.map((option) => (
                                <option key={option} value={option}>
                                    {option}
                                </option>
                            ))}
                        </select>
                    </label>
                )}

                <div className="data-table-pagination__pages">
                    <button
                        type="button"
                        className="data-table-pagination__button"
                        onClick={() => onPageChange(page - 1)}
                        disabled={!hasPrevious}
                        aria-label="Previous page"
                    >
                        <ChevronLeft size={17} aria-hidden="true" />
                    </button>

                    <span className="data-table-pagination__current">
                        {page + 1}
                        <span>/</span>
                        {totalPages}
                    </span>

                    <button
                        type="button"
                        className="data-table-pagination__button"
                        onClick={() => onPageChange(page + 1)}
                        disabled={!hasNext}
                        aria-label="Next page"
                    >
                        <ChevronRight size={17} aria-hidden="true" />
                    </button>
                </div>
            </div>
        </div>
    );
}

export default DataTablePagination;