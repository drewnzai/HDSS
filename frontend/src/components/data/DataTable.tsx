import type { ReactNode } from "react";
import "./data-table.css";

export interface DataTableColumn<T> {
    key: string;
    header: string;
    render: (item: T) => ReactNode;
    className?: string;
}

interface DataTableProps<T> {
    columns: DataTableColumn<T>[];
    data: T[];
    getRowKey: (item: T, index: number) => string | number;

    isLoading?: boolean;
    emptyMessage?: string;
    loadingMessage?: string;

    onRowClick?: (item: T) => void;
}

function DataTable<T>({
    columns,
    data,
    getRowKey,
    isLoading = false,
    emptyMessage = "No records found.",
    loadingMessage = "Loading...",
    onRowClick,
}: DataTableProps<T>) {
    return (
        <div className="data-table-wrapper">
            <div className="data-table-scroll">
                <table className="data-table">
                    <thead>
                        <tr>
                            {columns.map((column) => (
                                <th
                                    key={column.key}
                                    className={column.className}
                                    scope="col"
                                >
                                    {column.header}
                                </th>
                            ))}
                        </tr>
                    </thead>

                    <tbody>
                        {isLoading ? (
                            <tr>
                                <td
                                    className="data-table__state"
                                    colSpan={columns.length}
                                >
                                    <span
                                        className="data-table__spinner"
                                        aria-hidden="true"
                                    />
                                    <span>{loadingMessage}</span>
                                </td>
                            </tr>
                        ) : data.length === 0 ? (
                            <tr>
                                <td
                                    className="data-table__state"
                                    colSpan={columns.length}
                                >
                                    {emptyMessage}
                                </td>
                            </tr>
                        ) : (
                            data.map((item, index) => (
                                <tr
                                    key={getRowKey(item, index)}
                                    className={
                                        onRowClick
                                            ? "data-table__row--clickable"
                                            : undefined
                                    }
                                    onClick={() => onRowClick?.(item)}
                                >
                                    {columns.map((column) => (
                                        <td
                                            key={column.key}
                                            className={column.className}
                                        >
                                            {column.render(item)}
                                        </td>
                                    ))}
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default DataTable;