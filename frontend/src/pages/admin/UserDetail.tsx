import { useParams, Link } from "react-router-dom";

function UserDetail() {
    const { username } = useParams<{ username: string }>();

    return (
        <div className="card">
            <span className="ledger-section__eyebrow">§ Admin — User Record</span>
            <h1 className="record-id">{username}</h1>
            <p>Full user details will be shown here.</p>
            <Link to="/admin/users" className="btn btn--ghost">
                Back to users
            </Link>
        </div>
    );
}

export default UserDetail;