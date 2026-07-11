interface Props {
    type: string;
}


export default function AccountTypeBadge({
    type
}: Props) {

    return (
        <span>
            {type}
        </span>
    );

}