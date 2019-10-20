package net.tobano.quitsmoking.app.viewholder;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.tobano.quitsmoking.app.R;
import net.tobano.quitsmoking.app.models.Post;
import net.tobano.quitsmoking.app.util.CircleTransform;

import org.joda.time.Period;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static net.tobano.quitsmoking.app.Application.getContext;


public class PostViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView authorView;
    public TextView badgeView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView;
    public ImageView authorImageView;
    public ImageView reportView;
    public ImageView commentView;
    public TextView numCommentsView;
    public TextView timeSincePostCreated;
    public LinearLayout llCategoryIndicator;

    public PostViewHolder(View itemView) {
        super(itemView);

        titleView = itemView.findViewById(R.id.post_title);
        authorView = itemView.findViewById(R.id.post_author);
        badgeView = itemView.findViewById(R.id.social_level);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.post_num_stars);
        bodyView = itemView.findViewById(R.id.post_body);
        authorImageView = itemView.findViewById(R.id.post_author_photo);
        reportView = itemView.findViewById(R.id.report);
        commentView = itemView.findViewById(R.id.comment);
        numCommentsView = itemView.findViewById(R.id.post_num_comment);
        timeSincePostCreated = itemView.findViewById(R.id.time_since_post_text);
        llCategoryIndicator = itemView.findViewById(R.id.category_layout);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener, View.OnClickListener reportClickListener) {
        titleView.setText(post.title);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(post.body);
        numCommentsView.setText(String.valueOf(post.commentCount));

        starView.setOnClickListener(starClickListener);
        reportView.setOnClickListener(reportClickListener);

        if (post.displayedAuthorPhotoURL) {
            Picasso.with(getContext()).load(post.authorPhotoURL).transform(new CircleTransform()).into(authorImageView);
        } else {
            Picasso.with(getContext()).load(R.drawable.ic_action_account_circle_40).transform(new CircleTransform()).into(authorImageView);
        }

        Date now = new Date();
        int days = (int)( (now.getTime() - (long)post.authorQuittingDate) / (1000 * 60 * 60 * 24));
        if (days < 31) {
            badgeView.setBackground(getContext().getResources().getDrawable(R.drawable.badge_square));
            badgeView.setText(String.valueOf(days));
        }
        else if (days < 365) {
            badgeView.setBackground(getContext().getResources().getDrawable(R.drawable.badge_circle));
            badgeView.setText(String.valueOf((long)days/30));
        }
        else {
            badgeView.setBackground(getContext().getResources().getDrawable(R.drawable.badge_star));
            badgeView.setText(String.valueOf((long)days/365));
        }

        timeSincePostCreated.setText(getPostCreatedSinceFormatted(post.dateCreated));
    }

    private String getPostCreatedSinceFormatted(Object dateCreated) {
        String result;

        Long time = (Long)dateCreated;
        Calendar dc = GregorianCalendar.getInstance();
        dc.setTimeInMillis(time);

        Calendar now = Calendar.getInstance();
        Calendar lastMinute = (Calendar)now.clone();
        lastMinute.add(Calendar.MINUTE, -1);
        Calendar lastHour = (Calendar)now.clone();
        lastHour.add(Calendar.HOUR, -1);
        Calendar lastDay = (Calendar)now.clone();
        lastDay.add(Calendar.DAY_OF_MONTH, -1);
        Calendar lastWeek = (Calendar)now.clone();
        lastWeek.add(Calendar.WEEK_OF_MONTH, -1);
        Calendar lastMonth = (Calendar)now.clone();
        lastMonth.add(Calendar.MONTH, -1);
        Calendar lastYear = (Calendar)now.clone();
        lastYear.add(Calendar.YEAR, -1);

        Resources r = getContext().getResources();

        Period p = new Period(dc.getTimeInMillis(), now.getTimeInMillis());
        if(dc.compareTo(lastMinute) >= 0){
            result = r.getString(R.string.justKnow);
        }
        else if(dc.compareTo(lastHour) >= 0){
            if (p.getMinutes() == 1)
                result = p.getMinutes() + " " + r.getString(R.string.unitMin);
            else
                result = p.getMinutes() + " " + r.getString(R.string.tvKwitterSinceMinutesLabel);
        }
        else if(dc.compareTo(lastDay) >= 0){
            if(p.getHours() == 1)
                result = p.getHours() + " " + r.getString(R.string.unitHour);
            else
                result = p.getHours() + " " + r.getString(R.string.tvKwitterSinceHoursLabel);
        }
        else if(dc.compareTo(lastWeek) >= 0){
            if(p.getDays() == 1)
                result = p.getDays() + " " + r.getString(R.string.unitDay);
            else
                result = p.getDays() + " " + r.getString(R.string.tvKwitterSinceDaysLabel);
        }
        else if(dc.compareTo(lastMonth) >= 0){
            if(p.getWeeks() == 1)
                result = p.getWeeks() + " " + r.getString(R.string.unitWeek);
            else
                result = p.getWeeks() + " " + r.getString(R.string.unitWeeks);
        }
        else if(dc.compareTo(lastYear) >= 0){
            if(p.getMonths() == 1)
                result = p.getMonths() + " " + r.getString(R.string.unitMonth);
            else
                result = p.getMonths() + " " + r.getString(R.string.unitMonths);
        }
        else{
            if(p.getYears() == 1)
                result = p.getYears() + " " + r.getString(R.string.unitOneYear);
            else
                result = p.getYears() + " " + r.getString(R.string.unitYears);
        }

        return result;
    }
}
