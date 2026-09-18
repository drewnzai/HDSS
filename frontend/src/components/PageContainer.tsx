import type { ReactNode } from 'react';

interface PageContainerProps {
    children: ReactNode;
    size?: 'default' | 'wide' | 'full';
}

function PageContainer({
    children,
    size = 'default',
}: PageContainerProps) {
    return (
        <div className={`page-container page-container--${size}`}>
            {children}
        </div>
    );
}

export default PageContainer;