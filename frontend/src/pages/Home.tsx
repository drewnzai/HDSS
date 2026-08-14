import { Link } from "react-router-dom";
import { useAppSelector } from "../store/hooks";
import { selectUsername } from "../store/AuthSlice";
import { useLogout } from "../store/useLogout";

function Home() {
    const username = useAppSelector(selectUsername);
    const handleLogout = useLogout();

    return (
        <div style={{ maxWidth: 480, margin: "4rem auto" }}>
            <h2>Home</h2>
            <p>Logged in as: {username}</p>
            <nav>
                <Link to="/dashboard">Go to Dashboard</Link>
            </nav>
            <button onClick={handleLogout}>Logout</button>
        </div>
    );
}

export default Home;