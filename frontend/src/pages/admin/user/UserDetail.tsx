import { useLocation, useNavigate, useParams } from "react-router-dom";
import RecordDetail from "../../../components/record-detail/RecordDetail";
import type { UserSummary } from "../../../models/UserSummary";
import { useGetUserByUsernameQuery } from "../../../redux/AdminUserApi";

interface LocationState {
    user?: UserSummary;
}

function UserDetail() {
    const { username } = useParams<{ username: string }>();
    const location = useLocation();
    const navigate = useNavigate();

    const stateUser = (location.state as LocationState | null)?.user;

    const {
        data: fetchedUser,
        isLoading,
        error,
    } = useGetUserByUsernameQuery(username!, {
        skip: !username || Boolean(stateUser),
    });

    const user = stateUser ?? fetchedUser;

    const handleBack = () => {
        navigate("/admin/users");
    };

    if (!user) {
        return (
            <RecordDetail
                title=""
                sections={[]}
                isLoading={isLoading}
                error={error ?? (!isLoading ? new Error("User not found") : undefined)}
                loadingMessage="Loading user..."
                errorMessage="Couldn't find that user."
                backLabel="Back to users"
                onBack={handleBack}
            />
        );
    }

    return (
        <RecordDetail
            eyebrow="User Record"
            title={user.username}
            description="View account details and access information for this user."
            backLabel="Back to users"
            onBack={handleBack}
            sections={[
                {
                    eyebrow: "Details",
                    fields: [
                        {
                            label: "First name",
                            value: user.firstName,
                        },
                        {
                            label: "Last name",
                            value: user.lastName,
                        },
                        {
                            label: "Email",
                            value: user.email,
                        },
                        {
                            label: "Status",
                            value: user.deleted ? (
                                <span className="record-detail__badge record-detail__badge--danger">
                                    Deleted
                                </span>
                            ) : user.enabled ? (
                                <span className="record-detail__badge record-detail__badge--success">
                                    Active
                                </span>
                            ) : (
                                <span className="record-detail__badge record-detail__badge--warning">
                                    Disabled
                                </span>
                            ),
                        },
                    ],
                },
            ]}
        />
    );
}

export default UserDetail;